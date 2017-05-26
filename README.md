Dropbox4Akka
=========================

A (WIP) Dropbox API implementation for Akka-Http supporting file streaming (both upload and download).

Related docs:
* [Dropbox http api](https://www.dropbox.com/developers/documentation/http/documentation#files-upload)
* [Akka Actors](http://doc.akka.io/docs/akka/current/scala/index-actors.html?_ga=2.251396678.1329762334.1495784275-451437860.1492100426)
* [Akka Streams](http://doc.akka.io/docs/akka/current/scala/stream/index.html?_ga=2.245942912.540690987.1495784296-451437860.1492100426)
* [Akka-http](http://doc.akka.io/docs/akka-http/current/scala/http/index.html?_ga=2.180958053.315310577.1495784328-451437860.1492100426)

Dependencies: akka, play json support, akka-http

## Usage
Provide your own actorsystem implementation(recommended), or use the default provided (not recommended)
Acquire an OAuth2 access token from [the dropbox developer website](https://www.dropbox.com/developers/apps).
Use this token to set an implicit Authorization header upon client creation.
An example using the default client(not recommended):

```$xslt
      implicit val auth: Authorization = Dropbox4Akka.getAuthHeader(<your token here>)
      import DefaultClient.executionContext
    
      val myClient = DefaultClient()
      
      val list = ListFolder(
        path = ""
      )
    
      myClient.listFolder(list)
```

An example with the client providing your own actorsystem
```$xslt
  implicit val auth: Authorization = Dropbox4Akka.getAuthHeader(<your token here>)

  implicit val actorSys = ActorSystem("mysys")
  implicit val materializer = ActorMaterializer()
  implicit val ec = actorSys.dispatcher

  val myClient = DropboxClient()

  val upload = UploadFile(
    path = "/hi.txt",
    autorename = true,
    mute = true
  )

  val list = ListFolder(
    path = ""
  )

  myClient.listFolder(list).map(x => println(x))
```

The latter is preferred as to not be running multiple different ActorSystems, as well as for configuration purposes.

### Notes
Aside from incomplete, for file uploads/downloads, the API is quirky and the base folder must be prepended with "/", but
the base folder for a listFolder request is the empty string.

When using uploadFile and downloadFile, start with "/".

### Contribute
Too small a repo for a PR yet.