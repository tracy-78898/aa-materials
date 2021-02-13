package com.raywenderlich.podplay.service

import com.raywenderlich.podplay.BuildConfig
import com.raywenderlich.podplay.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.w3c.dom.Node
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory

class RssFeedService {

  suspend fun getFeed(xmlFileURL: String): RssFeedResponse? {
    val service: FeedService

    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.BODY

    val client = OkHttpClient().newBuilder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)

    if (BuildConfig.DEBUG) {
      client.addInterceptor(interceptor)
    }
    client.build()

    val retrofit = Retrofit.Builder()
        .baseUrl("$xmlFileURL/")
//        .addConverterFactory(JaxbConverterFactory.create())
//        .addConverterFactory(Xml.asConverterFactory(contentType))
        .build()
    service = retrofit.create(FeedService::class.java)

    try {
      val result = service.getFeed("$xmlFileURL/")
      if (result.code() >= 400) {
        // TODO : // create an error from error body and return
        println("server error, ${result.code()}, ${result.errorBody()}")
        return null
      } else {
        var rssFeedResponse : RssFeedResponse? = null
        // return success result
//        println(result.body()?.string())
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        CoroutineScope(Dispatchers.Default).launch {
          kotlin.runCatching {
            val doc = dBuilder.parse(result.body()?.byteStream())
            val rss = RssFeedResponse(episodes = mutableListOf())
            domToRssFeedResponse(doc, rss)
            println(rss)
            rssFeedResponse = rss
          }
        }
        return rssFeedResponse
      }
    } catch (t: Throwable) {
      // TODO : create an error from throwable and return it
      println("error, ${t.localizedMessage}")
    }
    return null
  }

  private fun domToRssFeedResponse(node: Node,
                                   rssFeedResponse: RssFeedResponse) {
    // 1
    if (node.nodeType == Node.ELEMENT_NODE) {
      // 2
      val nodeName = node.nodeName
      val parentName = node.parentNode.nodeName
      // 3
      if (parentName == "channel") {
        // 4
        when (nodeName) {
          "title" -> rssFeedResponse.title = node.textContent
          "description" -> rssFeedResponse.description = node.textContent
          "itunes:summary" -> rssFeedResponse.summary = node.textContent
          "item" -> rssFeedResponse.episodes?.
          add(RssFeedResponse.EpisodeResponse())
          "pubDate" -> rssFeedResponse.lastUpdated =
              DateUtils.xmlDateToDate(node.textContent)
        }
      }
    }
    // 5
    val nodeList = node.childNodes
    for (i in 0 until nodeList.length) {
      val childNode = nodeList.item(i)
      // 6
      domToRssFeedResponse(childNode, rssFeedResponse)
    }
  }

}

interface FeedService {
  @Headers(
      "Content-Type: application/xml; charset=utf-8",
      "Accept: application/xml"
  )
  @GET
  suspend fun getFeed(@Url xmlFileURL: String): Response<ResponseBody>
}