package com.example.android_coroutine_practice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import com.example.android_coroutine_practice.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : BindingActivity<ActivityMainBinding>() {
    override fun getLayoutRes(): Int = R.layout.activity_main
    private val netDispatcher = newFixedThreadPoolContext(2, name = "IO")
    private val factory = DocumentBuilderFactory.newInstance()

    private val feeds = listOf(
        "https://www.npr.org/rss/rss.php?id=1001",
        "http://rss.cnn.com/rss/cnn_topstories.rss",
        "http://feeds.foxnews.com/foxnews/politics?format=xml",
        "htt://myNewsFeed"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        asyncLoadNews(netDispatcher)
    }

    private fun asyncLoadNews(dispatcher: CoroutineDispatcher) = GlobalScope.launch {
        val request = mutableListOf<Deferred<List<String>>>()

        feeds.mapTo(request) {
            asyncFetchHeadlines(it, dispatcher = dispatcher)
        }

        //deferred의 join() 경우, 예외가 전파되지 않는 반면 await()는 예외가 전파된다.
        request.forEach {
            it.join()
        }

        val headLines = request
            .filter { !it.isCancelled }
            .flatMap {
            it.getCompleted()
        }

        val failed = request
            .filter { it.isCancelled }
            .size

        launch(Dispatchers.Main) {
            binding.newsCount.text = "Found ${headLines.size} News in ${request.size - failed} feeds"

            if(failed > 0) {
                binding.warings.text = "Failed to fetch $failed feeds"
            }
        }
    }

    private fun asyncFetchHeadlines(feed: String, dispatcher: CoroutineDispatcher) =
        GlobalScope.async(dispatcher) {
            val builder = factory.newDocumentBuilder()
            val xml = builder.parse(feed)
            val news = xml.getElementsByTagName("channel").item(0)

            (0 until news.childNodes.length)
                .map { news.childNodes.item(it) }
                .filter { Node.ELEMENT_NODE == it.nodeType }
                .map { it as Element }
                .filter { "item" == it.tagName }
                .map {
                    it.getElementsByTagName("title").item(0).textContent
                }
        }
}