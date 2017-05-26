/**
 * Copyright 2017 Jose Cardona
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.jmcardon.dropbox.endpoints

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, RawHeader}
import play.api.libs.json.{Json, OFormat, Writes}

/**
 * Trait representing a dropbox api endpoint
 *
 * @tparam T
 */
trait DropboxEndpoint[T <: DropboxRequest] {
  def request(t: T)(implicit auth: Authorization): HttpRequest
}

object DropboxEndpoint {
  private val dropboxHeaderName = "Dropbox-API-Arg"

  /**
   * Returns an RPC endpoint request implementation
   *
   * @tparam T
   * @return
   */
  def toRPCEndpoint[T <: DropboxRequest : Writes]: DropboxEndpoint[T] = new DropboxEndpoint[T] {
    override def request(t: T)(implicit auth: Authorization): HttpRequest = HttpRequest(
      uri = t.endpoint,
      method = HttpMethods.POST,
      entity = HttpEntity(ContentTypes.`application/json`,Json.toJson(t).toString())
    ).withHeaders(auth)
  }

  /**
   * Returns a content endpoint request impl
   *
   * @tparam T
   * @return
   */
  def toContentEndpoint[T <: DropboxRequest : Writes]: DropboxEndpoint[T] = new DropboxEndpoint[T] {
    override def request(t: T)(implicit auth: Authorization): HttpRequest = HttpRequest(
      uri = t.endpoint,
      method = HttpMethods.POST
    ).withHeaders(auth, RawHeader(dropboxHeaderName, Json.toJson(t).toString()))
  }

}