package controllers

import play.api.mvc.PathBindable
import play.api.Play
import play.api.libs.Files
import java.net.JarURLConnection
import Play.current
import java.io.File
import scalax.io.Resource
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import java.util.zip.CRC32
import java.net.URL
import scala.language.implicitConversions

//Author : https://gist.github.com/adamrabung/4163100
//Updated by oxman to use crc32 and filename instead of lastmodified and query string
//modified to use guava Cache instead of 2.10 TrieMap
//original: https://github.com/jroper/play-versioned-assets-example/blob/master/app/controllers/VersionedAssets.scala
class AccessTimeCacheLoader extends CacheLoader[URL, Option[Long]] {
  @Override
  override def load(resource: URL) = resource.getProtocol match {
    case "jar" => {
      resource.getPath.split('!').drop(1).headOption.flatMap { fileNameInJar =>
        Option(resource.openConnection)
          .collect { case c: JarURLConnection => c }
          .flatMap(c => Option(c.getJarFile.getJarEntry(fileNameInJar.drop(1))))
          .map(_.getCrc())
          .filterNot(_ == -1)
      }
    }
    case _ => None
  }
}

object VersionedAssets {
  def at(file: VersionedAsset) = {
    Assets.at(file.path, "crc32-[0-9]+-".r.replaceFirstIn(file.file, ""))
  }
}

case class VersionedAsset(file: String, path: String = "/public")

object VersionedAsset {
  private val lastModifieds = CacheBuilder.newBuilder().maximumSize(10000).build(new AccessTimeCacheLoader());

  private def lastModifiedFor(resource: java.net.URL): Option[Long] = lastModifieds.get(resource)

  implicit def pathBinder = new PathBindable[VersionedAsset] {
    def bind(key: String, value: String) = Right(VersionedAsset(value))

    def unbind(key: String, value: VersionedAsset) = {
      val resourceName = Option(value.path + "/" + value.file).map(name => if (name.startsWith("/")) name else ("/" + name)).get
      val modified = Play.resource(resourceName).flatMap { resource =>
        resource.getProtocol match {
          case file if file == "file" => {
            val crc32Object = new CRC32
            val stringArray = Resource.fromFile(resource.getFile).byteArray
            crc32Object.update(stringArray)
            Some(crc32Object.getValue)
          }
          case jar if jar == "jar" => lastModifiedFor(resource)
          case _ => None
        }
      }

      var file = new File(value.file)
      modified.map(file.getParentFile + "/crc32-" + _ + "-" + file.getName).getOrElse(value.file)
    }
  }

  implicit def toVersionedAsset(path: String): VersionedAsset = VersionedAsset(path)
}
