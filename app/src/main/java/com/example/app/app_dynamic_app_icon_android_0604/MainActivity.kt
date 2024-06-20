package com.example.app.app_dynamic_app_icon_android_0604

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton

class MainActivity : Activity() {
    private var iconChangeManager: IconChangeManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        iconChangeManager = IconChangeManager(this)

        val output = findViewById<TextView>(R.id.text_output)
        val activateButton = findViewById<ToggleButton>(R.id.button_activate)

        output.text = getString(
            R.string.current_alias,
            iconChangeManager!!.getCurrentAlias().simpleName
        )

        activateButton.isChecked = iconChangeManager!!.isIconChangeActivated
        activateButton.setOnCheckedChangeListener { v: CompoundButton?, checked: Boolean ->
            switchActivation(
                checked
            )
        }

        if (iconChangeManager!!.isIconChangeActivated) {
            val gridView = findViewById<GridView>(R.id.grid_view)
            gridView.adapter = AliasAdapter(this, iconChangeManager!!)
            gridView.onItemClickListener =
                OnItemClickListener { av: AdapterView<*>?, v: View?, p: Int, id: Long ->
                    onAliasSelected(
                        p
                    )
                }
        }

        if (iconChangeManager!!.isIconChangeRefresh(savedInstanceState)) {
            showIconChangeMessage()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        iconChangeManager!!.onSaveInstanceState(outState)
    }

    private fun switchActivation(enable: Boolean) {
        findViewById<View>(android.R.id.content).visibility = View.GONE
        if (enable) {
            iconChangeManager!!.activateIconChange()
        } else {
            if (!iconChangeManager!!.deactivateIconChange()) {
                Toast.makeText(this, R.string.deactivate_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onAliasSelected(position: Int) {
        iconChangeManager!!.setCurrentAlias(iconChangeManager!!.aliases[position])
    }

    private fun showIconChangeMessage() {
        val message = if (iconChangeManager!!.isIconChangeActivated) getString(
            R.string.message_alias_selected,
            iconChangeManager!!.getCurrentAlias().simpleName
        ) else getString(R.string.message_alias_reset)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private class AliasAdapter(context: Context, manager: IconChangeManager) :
        ArrayAdapter<Alias?>(context, R.layout.item_alias, manager.aliases) {
        private val currentAlias = manager.getCurrentAlias()
        private val drawableDimen =
            context.resources.getDimensionPixelSize(R.dimen.item_alias_image_size)

        @SuppressLint("UseCompatLoadingForDrawables")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent) as TextView
            val drawable = context.getDrawable(getItem(position)!!.iconResId)
            drawable!!.setBounds(0, 0, drawableDimen, drawableDimen)
            view.setCompoundDrawables(null, drawable, null, null)
            view.alpha = if (isEnabled(position)) 1f else .3f
            return view
        }

        override fun isEnabled(position: Int): Boolean {
            return !getItem(position)!!.equals(currentAlias)
        }
    }
}