/**
 * Copyright 2017 Jose Cardona
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.jmcardon.dropbox.endpoints

import play.api.libs.json._

sealed trait DropboxRequest {
  val dropboxUrl : String
  def endpoint: String = DropboxRequest.dropboxApiEndpoint + dropboxUrl
}

object DropboxRequest {
  val dropboxApiEndpoint = "https://api.dropboxapi.com/2/"
  val dropboxContentEndpoint = "https://content.dropboxapi.com/2/"
}

/**
 * List folder event
 *
 * @param path
 * @param recursive
 * @param include_media_info
 * @param include_deleted
 */
case class ListFolder(
                       path: String,
                       recursive: Boolean = false,
                       include_media_info: Boolean = false,
                       include_deleted: Boolean = false
                     ) extends DropboxRequest {
  val dropboxUrl = "files/list_folder"
}
//Companion object
object ListFolder {
  implicit val format = Json.format[ListFolder]
  implicit val dbendpoint = DropboxEndpoint.toRPCEndpoint[ListFolder]
}

case class ListFolderContinue(cursor: String) extends DropboxRequest {
  override val dropboxUrl: String = "files/list_folder/continue"
}

object ListFolderContinue {
  implicit val format = Json.format[ListFolderContinue]
  implicit val dbendpoint = DropboxEndpoint.toRPCEndpoint[ListFolderContinue]
}

/**
 * The search enum modes
 *
 */
object SearchMode extends Enumeration {
  type SearchMode = Value
  val filename, filename_and_content, deleted_filename = Value
}

/**
 * Search for file event
 *
 * @param path
 * @param query
 * @param start
 * @param max_results
 * @param mode
 */
case class Search(
                   path: String,
                   query: String,
                   start: Int = 0,
                   max_results: Int = 100,
                   mode: String = "filename"
                 ) extends DropboxRequest {
  override val dropboxUrl: String = "files/search"
}

object Search {
  implicit val format = Json.format[Search]
  implicit val dbendpoint = DropboxEndpoint.toRPCEndpoint[Search]
}

/**
 * Class to Delete files
 *
 * @param path
 */
case class FileDelete(path: String) extends DropboxRequest {
  override val dropboxUrl: String = "files/search"
}

object FileDelete {
  implicit val format = Json.format[FileDelete]
  implicit val dbendpoint = DropboxEndpoint.toRPCEndpoint[FileDelete]
}

/**
 * Creates a folder in dropbox
 *
 * @param path
 * @param autorename
 */
case class CreateFolder(path: String, autorename: Boolean = false) extends DropboxRequest {
  override val dropboxUrl: String = "files/delete"
}

object CreateFolder {
  implicit val format = Json.format[CreateFolder]
  implicit val dbendpoint = DropboxEndpoint.toRPCEndpoint[CreateFolder]
}

/**
 * The write mode repr.
 * Type alias is necessary for representing a union type
 *
 */
sealed trait WriteMode {
  type WriteType
  val mode: WriteType
}

object WriteMode {

  /**
   * Writemode Add
   */
  case object Add extends WriteMode {
    override type WriteType = String
    override val mode: String = "add"
  }

  /**
   *
   */
  case object Overwrite extends WriteMode {
    override type WriteType = String
    override val mode: String = "overwrite"
  }

  final case class Update(rev: String) extends WriteMode {
    import Update._
    override type WriteType = UpdateWrite
    override val mode: UpdateWrite = UpdateWrite("update", rev)
  }

  object Update {
    case class UpdateWrite(`.tag`: String, update: String)
    implicit val writeFormat = Json.format[UpdateWrite]
    implicit val format = Json.format[Update]
  }

  implicit val format = new Writes[WriteMode] {
    override def writes(o: WriteMode): JsValue = o match {
      case update : Update => Json.toJson(update.mode)
      case Add => JsString(Add.mode)
      case Overwrite => JsString(Overwrite.mode)
    }
  }
  val add: WriteMode = Add
  val overwrite: WriteMode = Overwrite

  /**
   * Returns the update object to serialize
   *
   * @param rev
   * @return
   */
  def update(rev: String) = Update(rev)

}

/**
 * The uploadRequest
 *
 * @param path
 * @param mode
 * @param autorename
 * @param mute
 */
case class UploadFile(path: String, mode: WriteMode = WriteMode.add, autorename: Boolean, mute: Boolean) extends DropboxRequest {
  override val dropboxUrl: String = "files/upload"
  override def endpoint: String = DropboxRequest.dropboxContentEndpoint + dropboxUrl
}


/**
 * Dropbox api endpoint upload model
 */
object UploadFile {
  implicit val format: OWrites[UploadFile] = Json.writes[UploadFile]
  implicit val dbendpoint = DropboxEndpoint.toContentEndpoint[UploadFile]
}

/**
 * Dropbox api endpoint download model
 *
 * @param path
 */
case class DownloadFile(path: String) extends DropboxRequest {
  override val dropboxUrl: String = "files/download"
  override def endpoint: String = DropboxRequest.dropboxContentEndpoint + dropboxUrl
}

object DownloadFile {
  implicit val format: OFormat[DownloadFile] = Json.format[DownloadFile]
  implicit val dbendpoint = DropboxEndpoint.toContentEndpoint[DownloadFile]
}

/**
 * Get Metadata event
 *
 * @param path
 * @param include_media_info
 * @param include_deleted
 * @param include_has_explicit_shared_members
 */
case class GetMetadata(
                        path: String,
                        include_media_info: Boolean = false,
                        include_deleted: Boolean = false,
                        include_has_explicit_shared_members: Boolean = false
                      ) extends DropboxRequest {
  override val dropboxUrl: String = "files/get_metadata"
}

object GetMetadata {

  implicit val format = Json.format[GetMetadata]
  implicit val dbEndpoint = DropboxEndpoint.toRPCEndpoint[GetMetadata]

}
