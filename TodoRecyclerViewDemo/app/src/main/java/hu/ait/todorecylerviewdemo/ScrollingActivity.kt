package hu.ait.todorecylerviewdemo

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import hu.ait.todorecylerviewdemo.R
import hu.ait.todorecylerviewdemo.TodoDialog
import hu.ait.todorecylerviewdemo.adapter.TodoAdapter
import hu.ait.todorecylerviewdemo.data.AppDatabase
import hu.ait.todorecylerviewdemo.data.Todo
import hu.ait.todorecylerviewdemo.touch.TodoReyclerTouchCallback
import kotlinx.android.synthetic.main.activity_scrolling.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.util.*

class ScrollingActivity : AppCompatActivity(), TodoDialog.TodoHandler {

    lateinit var todoAdapter: TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)

        setSupportActionBar(toolbar)

        initRecyclerView()

        fab.setOnClickListener {
            showAddTodoDialog()
        }

        fabDeleteAll.setOnClickListener {
            todoAdapter.deleteAllTodos()
        }

        if(!wasStartedBefore()) {
            MaterialTapTargetPrompt.Builder(this)
                .setTarget(R.id.fab)
                .setPrimaryText("New item")
                .setSecondaryText("Click here to create new items")
                .show()
            saveWasStarted()
        }
    }

    private fun initRecyclerView() {
        Thread {
            var todos = AppDatabase.getInstance(this@ScrollingActivity).todoDao().getAllTodo()

            runOnUiThread {
                todoAdapter = TodoAdapter(this, todos)
                recyclerTodo.adapter = todoAdapter

                var itemDecorator = DividerItemDecoration(
                    this,
                    DividerItemDecoration.VERTICAL
                )
                recyclerTodo.addItemDecoration(itemDecorator)

                //recyclerTodo.layoutManager = GridLayoutManager(this,
                //    2)

                val callback = TodoReyclerTouchCallback(todoAdapter)
                val touchHelper = ItemTouchHelper(callback)
                touchHelper.attachToRecyclerView(recyclerTodo)
            }

        }.start()

    }

    fun showAddTodoDialog() {
        TodoDialog().show(supportFragmentManager, "TAG_TODO_DIALOG")
    }

    var editIndex: Int = -1

    fun showEditTodoDialog(todoToEdit: Todo, idx: Int) {
        editIndex = idx


        val editDialog = TodoDialog()

        var bundle = Bundle()
        bundle.putSerializable("KEY_TODO", todoToEdit)

        editDialog.arguments = bundle

        editDialog.show(supportFragmentManager, "TAG_TODO_EDIT")
    }


    fun saveTodo(todo: Todo) {
        Thread {
            todo.todoId = AppDatabase.getInstance(this@ScrollingActivity).todoDao().addTodo(todo)

            runOnUiThread {
                todoAdapter.addTodo(todo)
            }

        }.start()
    }

    override fun todoCreated(item: Todo) {
        saveTodo(item)
    }

    override fun todoUpdated(item: Todo) {
        Thread{
            AppDatabase.getInstance(this@ScrollingActivity).todoDao().updateTodo(item)
            runOnUiThread {
                todoAdapter.updateTodoOnPosition(item, editIndex)
            }
        }.start()
    }

    fun saveWasStarted() {
        var sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@ScrollingActivity)
        sharedPref.edit().putBoolean("KEY_STARTED", true).apply()
    }

    fun wasStartedBefore(): Boolean {

        var sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@ScrollingActivity)


        return sharedPref.getBoolean("KEY_STARTED", false)
    }

}