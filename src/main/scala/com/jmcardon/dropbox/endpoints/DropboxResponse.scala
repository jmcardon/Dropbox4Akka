/**
 * Copyright 2017 Jose Cardona
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.jmcardon.dropbox.endpoints

import play.api.libs.json.Reads._
import play.api.libs.json._

sealed trait DropboxResponse


/**
 * Trait representing all metadata types for a dropbox api endpoint
 *
 */
sealed trait DropboxMetadata

object DropboxMetadata {
  val folderTag = "folder"
  val fileTag = "file"
  val deletedTag = "deleted"

  implicit val format = new Format[DropboxMetadata] {
    override def writes(o: DropboxMetadata): JsValue = o match {
      case f: FileMetadata => Json.toJson(f)
      case d: DeletedMetadata => Json.toJson(d)
      case f: FolderMetadata => Json.toJson(f)
    }

    override def reads(json: JsValue): JsResult[DropboxMetadata] = for {
      obj <- json.validate[JsObject]
      tag <- obj.value(".tag").validate[String]
      metaData <- tag match {
        case DropboxMetadata.folderTag => obj.validate[FolderMetadata]
        case DropboxMetadata.deletedTag => obj.validate[DeletedMetadata]
        case DropboxMetadata.fileTag => obj.validate[FileMetadata]
        case _ => JsError("Invalid tag")
      }
    } yield metaData
  }
}
/**
 * Class modeling response
 *
 * @param entries
 * @param cursor
 * @param has_more
 */
case class ListFolderResponse(entries: Vector[DropboxMetadata], cursor: Option[String], has_more: Boolean) extends DropboxResponse

object ListFolderResponse {

  implicit val listFolderResponseFormat: OFormat[ListFolderResponse] = Json.format[ListFolderResponse]

}

/**
 * Sharing info
 *
 * @param read_only
 * @param parent_shared_folder_id
 * @param modified_by
 */
final case class SharingInfo(read_only: Boolean, parent_shared_folder_id: Long,
                             modified_by: String)

object SharingInfo {
  implicit val sharingInfoFormat = Json.format[SharingInfo]
}

/**
 * Dropbox PropertyGroups
 *
 * @param template_id
 * @param fields
 */
final case class PropertyGroups(template_id: String, fields: List[PropertyGroups.PropertyGroupField])

object PropertyGroups {
  final case class PropertyGroupField(name: String, value: String)

  object PropertyGroupField {
    implicit val format: OFormat[PropertyGroupField] = Json.format[PropertyGroupField]
  }

  implicit val propertyGroupsFormat: OFormat[PropertyGroups] = Json.format[PropertyGroups]
}

/**
 * FileMetadata case class repr
 *
 * @param name name
 * @param id dropbox id
 * @param client_modified
 * @param server_modified
 * @param rev
 * @param size
 * @param path_lower
 * @param path_display
 * @param sharing_info
 * @param property_groups
 * @param has_explicit_shared_members
 * @param content_hash
 */
final case class FileMetadata(name: String,
                        id: String,
                        client_modified: Option[String],
                        server_modified: Option[String],
                        rev: Option[String],
                        size: Int,
                        path_lower: String,
                        path_display: String,
                        sharing_info: Option[SharingInfo],
                        property_groups: Option[List[PropertyGroups]],
                        has_explicit_shared_members: Option[Boolean],
                        content_hash: Option[String]
                       ) extends DropboxResponse with DropboxMetadata

object FileMetadata {
  implicit val format: OFormat[FileMetadata] = Json.format[FileMetadata]
}

/**
 * FolderMetadata case class repr
 *
 * @param name
 * @param id
 * @param path_lower
 * @param path_display
 * @param sharing_info
 * @param property_groups
 */
final case class FolderMetadata(name: String,
                          id: String,
                          path_lower: String,
                          path_display: String,
                          sharing_info: Option[SharingInfo],
                          property_groups: Option[List[PropertyGroups]]
                         )  extends DropboxResponse with DropboxMetadata

object FolderMetadata {
  implicit val format: OFormat[FolderMetadata] = Json.format[FolderMetadata]
}

/**
 * DeltedMetadata case class repr
 *
 * @param name
 * @param path_lower
 * @param path_display
 */
final case class DeletedMetadata(name: String,
                                 path_lower: Option[String],
                                 path_display: Option[String]) extends DropboxResponse with DropboxMetadata

object DeletedMetadata {
  implicit val format: OFormat[DeletedMetadata] = Json.format[DeletedMetadata]
}
