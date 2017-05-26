/**
 * Copyright 2017 Jose Cardona
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.jmcardon.dropbox.client

import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.stream.{ActorMaterializer, Materializer}
import com.jmcardon.dropbox.endpoints._
import java.nio.file.{Paths => JPaths}

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.RejectionError
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshal}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.{JsObject, OFormat, Writes}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by jose on 26/05/17.
 */
object Dropbox4Akka {

  /**
   * Returns an auth token from a string
   *
   * @param token
   * @return
   */
  def getAuthHeader(token: String): Authorization = Authorization(OAuth2BearerToken(token))


  trait DropboxClient extends PlayJsonSupport {
    implicit val authorization: Authorization
    implicit val actorSystem: ActorSystem
    implicit val materializer: Materializer
    implicit val ec: ExecutionContext

    /**
     * Upload a file sub 150Mb to dropbox
     *
     * @param uploadOptions
     * @param file
     * @return
     */
    def uploadFile(uploadOptions: UploadFile, file: File): Future[Either[FileMetadata, Exception]]

    /**
     * Download a file
     *
     * @param downloadOptions
     * @return
     */
    def downloadFile(downloadOptions: DownloadFile): Future[Either[Source[ByteString,Any], Exception]]

    /**
     * Create a folder
     *
     * @param createFolder
     * @return
     */
    def createFolder(createFolder: CreateFolder): Future[Either[FolderMetadata,Exception]]


    /**
     * ListFolder API endpoint
     *
     * @param listFolder
     * @return
     */
    def listFolder(listFolder: ListFolder): Future[Either[ListFolderResponse, Exception]]

  }


  /**
   * The default dropbox client implementation
   *
   * @param authorization
   * @param actorSystem
   * @param materializer
   * @param ec
   */
  final class DefaultDropboxClient()(implicit val authorization: Authorization,
                                     val actorSystem: ActorSystem,
                                     val materializer: Materializer,
                                     val ec: ExecutionContext) extends DropboxClient{
    private val client = Http(actorSystem)

    /**
     * Any dropbox file RPC request implementation
     *
     * @param requestEndpoint
     * @tparam T
     * @return
     */
    private def dropboxRPCRequest[T <: DropboxRequest : Writes : DropboxEndpoint](requestEndpoint : T): Future[HttpResponse] =  {
      val request: HttpRequest = implicitly[DropboxEndpoint[T]].request(requestEndpoint)
      client.singleRequest(request)
    }

    /**
     * Dropbox upload request.
     * Handled in a special method as it is the only one that requires a different request entity
     * than JSON in the body or
     * just header JSON
     *
     * @param requestEndpoint
     * @param file
     * @param p
     * @return
     */
    private def dropboxUploadRequest(requestEndpoint: UploadFile, file: File)(implicit p: DropboxEndpoint[UploadFile]): Future[HttpResponse] = {
      val request = p.request(requestEndpoint).withEntity(HttpEntity.fromPath(ContentTypes.`application/octet-stream`,JPaths.get(file.getAbsolutePath)))
      client.singleRequest(request)
    }

    /**
     * Upload a file sub 150Mb to dropbox
     *
     * @param uploadOptions
     * @param file
     * @return
     */
    override def uploadFile(uploadOptions: UploadFile, file: File): Future[Either[FileMetadata, Exception]] =
      dropboxUploadRequest(uploadOptions, file).flatMap{
        response =>
          Unmarshal(response.entity)
            .to[FileMetadata].map(Left(_))
      }.recover{
        case e: Exception => Right(e)
      }

    /**
     * Download a file
     *
     * @param downloadOptions
     * @return
     */
    override def downloadFile(downloadOptions: DownloadFile): Future[Either[Source[ByteString, Any], Exception]] =
      dropboxRPCRequest(downloadOptions).map(
        response =>
          if(response.status == StatusCodes.OK)
            Left(response.entity.dataBytes)
          else
            Right(new NoSuchElementException)
      ).recover{
        case e: Exception => Right(e)
      }

    /**
     * Sugar to repeat the RPC request pattern
     *
     * @param rpc
     * @param unmarshaller
     * @tparam T
     * @tparam K
     * @return
     */
    private def rpcRequest[T <: DropboxRequest : Writes : DropboxEndpoint, K<: DropboxResponse](rpc: T)(implicit unmarshaller: FromEntityUnmarshaller[K]): Future[Either[K, Exception]] =
      dropboxRPCRequest(rpc).flatMap{
        response =>
          Unmarshal(response.entity).to[K].map(Left(_))
      }.recover{
        case e: Exception => Right(e)
      }

    /**
     * Create a folder
     *
     * @param createFolder
     * @return
     */
    override def createFolder(createFolder: CreateFolder): Future[Either[FolderMetadata, Exception]] =
      rpcRequest[CreateFolder, FolderMetadata](createFolder)

    /**
     * ListFolder API endpoint
     *
     * @param listFolder
     * @return
     */
    override def listFolder(listFolder: ListFolder): Future[Either[ListFolderResponse, Exception]] =
      rpcRequest[ListFolder, ListFolderResponse](listFolder)
  }

  /**
   * A default client object.
   * NOTE: Do not use
   *
   */
  object DefaultClient {
    implicit lazy val actorSystem = ActorSystem("dropbox")
    implicit lazy val materializer = ActorMaterializer()
    implicit lazy val executionContext = actorSystem.dispatcher


    def apply()(implicit auth: Authorization) = new DefaultDropboxClient()
  }

  /**
   * The recommended client singleton
   *
   */
  object DropboxClient {

    def apply()(implicit auth: Authorization, ac: ActorSystem, executionContext: ExecutionContext,
                materializer: Materializer) = new DefaultDropboxClient()
  }

}
