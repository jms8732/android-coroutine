package com.example.android_coroutine_practice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.android_coroutine_practice.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : BindingActivity<ActivityMainBinding>() {
    override fun getLayoutRes(): Int = R.layout.activity_main
    private val netDispatcher = newSingleThreadContext(name = "ServiceCall")
    private val factory = DocumentBuilderFactory.newInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlobalScope.launch(netDispatcher) {
            val headlines = fetchResHeadlines()

            GlobalScope.launch (Dispatchers.Main){
                binding.newsCount.text = "Found ${headlines.size} News"
            }
        }
    }

    private fun fetchResHeadlines() : List<String>{
        val builder = factory.newDocumentBuilder()
        val xml = builder.parse("https://www.npr.org/rss/rss.php?id=1001")
        val news = xml.getElementsByTagName("channel").item(0)

        return (0 until news.childNodes.length)
            .map { news.childNodes.item(it) }
            .filter { Node.ELEMENT_NODE == it.nodeType }
            .map { it as Element }
            .filter { "item" == it.tagName }
            .map {
                it.getElementsByTagName("title").item(0).textContent
            }
    }
}