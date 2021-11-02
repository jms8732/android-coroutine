package com.example.android_coroutine

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_coroutine.adapter.ArticleAdapter
import com.example.android_coroutine.databinding.ActivityMainBinding
import com.example.android_coroutine.model.Article
import com.example.android_coroutine.model.Feed
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : BindingActivity<ActivityMainBinding>() {
    override fun getLayoutRes(): Int = R.layout.activity_main
    private val netDispatcher = newFixedThreadPoolContext(2, name = "IO")
    private val factory = DocumentBuilderFactory.newInstance()
    private lateinit var articleAdapter: ArticleAdapter

    private val feeds = listOf(
        Feed("npr","https://www.npr.org/rss/rss.php?id=1001"),
        Feed("cnn","http://rss.cnn.com/rss/cnn_topstories.rss"),
        Feed("fox","http://feeds.foxnews.com/foxnews/politics?format=xml"),
        Feed("inv","htt://myNewsFeed")
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.articles.run {
            layoutManager = LinearLayoutManager(this@MainActivity)
            articleAdapter = ArticleAdapter()
            adapter = articleAdapter
        }

        asyncLoadNews(netDispatcher)
    }

    private fun asyncLoadNews(dispatcher: CoroutineDispatcher) = GlobalScope.launch {
        val request = mutableListOf<Deferred<List<Article>>>()

        feeds.mapTo(request) {
            asyncFetchArticles(it, dispatcher = dispatcher)
        }

        //deferred의 join() 경우, 예외가 전파되지 않는 반면 await()는 예외가 전파된다.
        request.forEach {
            it.join()
        }

        val articles = request
            .filter { !it.isCancelled }
            .flatMap {
                it.getCompleted()
            }

        val failed = request
            .filter { it.isCancelled }
            .size

        launch(Dispatchers.Main) {
            //TODO: UI 갱신
            binding.progressBar.visibility = View.GONE
            articleAdapter.add(articles)
        }
    }

    private fun asyncFetchArticles(feed: Feed, dispatcher: CoroutineDispatcher) =
        GlobalScope.async(dispatcher) {
            delay(1000L)
            val builder = factory.newDocumentBuilder()
            val xml = builder.parse(feed.url)
            val news = xml.getElementsByTagName("channel").item(0)

            (0 until news.childNodes.length)
                .map { news.childNodes.item(it)}
                .filter { Node.ELEMENT_NODE == it.nodeType }
                .map { it as Element }
                .filter { "item" == it.tagName }
                .map {
                    val title = it.getElementsByTagName("title").item(0).textContent
                    var summary = it.getElementsByTagName("description").item(0).textContent

                    if(!summary.startsWith("<div") && summary.contains("<div")){
                        summary = summary.substring(0,summary.indexOf("<div"))
                    }

                    Article(feed.name,title,summary)
                }
        }
}