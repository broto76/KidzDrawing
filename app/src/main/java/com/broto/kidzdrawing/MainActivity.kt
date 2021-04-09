package com.broto.kidzdrawing

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.text.set
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.color_picker_popup.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import kotlinx.android.synthetic.main.dialog_brush_size_slider.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private var mImageButtonCurrentPaint: ImageButton? = null
    private var mBrushSize = 20
    private var mColor = "#000000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        drawing_view.setSizeForBrush(22.toFloat())

//        mImageButtonCurrentPaint = ll_paint_colors[1] as ImageButton
//        mImageButtonCurrentPaint!!.setImageDrawable(
//            ContextCompat.getDrawable(this,
//                R.drawable.pallet_pressed)
//        )

        ib_brush.setOnClickListener{
            //showBrushSizeChooserDialog()
            showBrushSizeSliderDialog()
        }

        ib_gallery.setOnClickListener {
            if (isReadStorageAllowed()) {
                val pickPhotoIntent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhotoIntent, GALLERY)
            } else {
                requestStoragePermission()
            }
        }


        ib_color_picker.setOnClickListener {
            showColorPickerDialog()
        }

        ib_redo.setOnClickListener {
            drawing_view.redo()
        }

        ib_save.setOnClickListener {
            if (isReadStorageAllowed()) {
                BitmapAsyncTask(getBitmapFromView(drawing_view_container)).execute()
            } else {
                requestStoragePermission()
            }
        }

        ib_clear.setOnLongClickListener {
            // On long click clear the canvas
            drawing_view.clearDrawing()
            iv_background.setImageDrawable(Color.WHITE.toDrawable())
            drawing_view.invalidate()
            return@setOnLongClickListener true
        }

        ib_clear.setOnClickListener {
            // On normal click just undo
            drawing_view.undo()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY) {
            try {
                if(data?.data != null) {
                    iv_background.visibility = View.VISIBLE
                    iv_background.setImageURI(data.data)
                } else {
                    Toast.makeText(this, "Unable to set background",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            // Make the Activity go Full Screen
            window.decorView.apply {
                systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            }
            supportActionBar?.hide()
        }
        super.onWindowFocusChanged(hasFocus)
    }

    private fun showBrushSizeChooserDialog() {
        // Deprecated
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size: ")

        brushDialog.ib_small_brush.setOnClickListener{
            drawing_view.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.ib_medium_brush.setOnClickListener{
            drawing_view.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.ib_large_brush.setOnClickListener{
            drawing_view.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

    private fun showBrushSizeSliderDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size_slider)
        brushDialog.setTitle("Brush Size: ")
        brushDialog.size_slider.progress = mBrushSize
        brushDialog.tv_size.text = mBrushSize.toString()

        brushDialog.size_slider.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.d("Broto", "Progress: $progress")
                mBrushSize = progress
                brushDialog.tv_size.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                brushDialog.dismiss()
                drawing_view.setSizeForBrush(mBrushSize.toFloat())
            }

        })

        brushDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        brushDialog.show()
    }

    private fun showColorPickerDialog() {

        var red = mColor.substring(1,3)
        var green = mColor.substring(3,5)
        var blue = mColor.substring(5)

        Log.d(TAG, "Red: $red")
        Log.d(TAG, "Green: $green")
        Log.d(TAG, "Blue: $blue")


        val colorPicker = Dialog(this)
        colorPicker.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        colorPicker.setContentView(R.layout.color_picker_popup)

        colorPicker.rl_color_view.background = Color.parseColor(mColor).toDrawable()

        colorPicker.et_color_value.setText(mColor)
        colorPicker.et_color_value.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length == 7) {
                    // Check value is within FFFFFF
                    //Log.d("Broto","Color: ${s.toString()} int: ${java.lang.Long.parseLong(s.toString().substring(1), 16)}")
                    try {
                        colorPicker.rl_color_view.background = Color.parseColor(s.toString()).toDrawable()
                        mColor = s.toString()

                        // Update the seekbars
//                        colorPicker.seekbar_red.progress = Integer.parseInt(mColor.substring(1,3),16)
//                        colorPicker.seekbar_green.progress = Integer.parseInt(mColor.substring(3,5),16)
//                        colorPicker.seekbar_blue.progress = Integer.parseInt(mColor.substring(5),16)

                        colorPicker.bt_pick_color.visibility = View.VISIBLE
                    } catch (e: IllegalArgumentException) {
                        mColor = "NA"
                        colorPicker.bt_pick_color.visibility = View.INVISIBLE
                        Log.e(TAG, "Invalid Color.")
                        e.printStackTrace()
                    }
                } else {
                    colorPicker.bt_pick_color.visibility = View.INVISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        colorPicker.seekbar_red.progress = Integer.parseInt(red,16)
        colorPicker.seekbar_red.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val newColor = "#" +
                        progress.toString(16).padStart(2,'0') +
                        colorPicker.seekbar_green.progress.toString(16).padStart(2,'0') +
                        colorPicker.seekbar_blue.progress.toString(16).padStart(2, '0')
                colorPicker.et_color_value.setText(newColor)
                Log.d(TAG, "From Red, progress: $progress hexprogress: ${progress.toString(16).padStart(2,'0')} newColor: $newColor")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        colorPicker.seekbar_green.progress = Integer.parseInt(green,16)
        colorPicker.seekbar_green.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val newColor = "#" +
                        colorPicker.seekbar_red.progress.toString(16).padStart(2,'0') +
                        progress.toString(16).padStart(2,'0') +
                        colorPicker.seekbar_blue.progress.toString(16).padStart(2, '0')
                colorPicker.et_color_value.setText(newColor)
                Log.d(TAG, "From Green, progress: $progress hexprogress: ${progress.toString(16).padStart(2,'0')} newColor: $newColor")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        colorPicker.seekbar_blue.progress = Integer.parseInt(blue,16)
        colorPicker.seekbar_blue.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val newColor = "#" +
                        colorPicker.seekbar_red.progress.toString(16).padStart(2,'0') +
                        colorPicker.seekbar_green.progress.toString(16).padStart(2,'0') +
                        progress.toString(16).padStart(2,'0')
                colorPicker.et_color_value.setText(newColor)
                Log.d(TAG, "From Blue, progress: $progress hexprogress: ${progress.toString(16).padStart(2,'0')} newColor: $newColor")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        colorPicker.bt_pick_color.setOnClickListener {
            if (mColor != "NA") {
                drawing_view.setColorForBrush(mColor)
                ib_color_picker.setBackgroundColor(Color.parseColor(mColor))
                colorPicker.dismiss()
            }
        }

        colorPicker.setCancelable(false)
        colorPicker.show()
    }

//    fun paintClicked(view: View) {
//        if (view != mImageButtonCurrentPaint) {
//            val imageButton = view as ImageButton
//            val colorTag = imageButton.tag.toString()
//            drawing_view.setColorForBrush(colorTag)
//            imageButton.setImageDrawable(
//                ContextCompat.getDrawable(this,
//                    R.drawable.pallet_pressed)
//            )
//
//            mImageButtonCurrentPaint!!.setImageDrawable(
//                ContextCompat.getDrawable(this,
//                    R.drawable.pallet_normal)
//            )
//            mImageButtonCurrentPaint = imageButton
//        }
//    }

    private fun requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())) {
            Toast.makeText(this, "Need Permission to add Background",
                Toast.LENGTH_SHORT).show()
        }

        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission for storage Granted",
                    Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Cannot set custom background without permission",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isReadStorageAllowed(): Boolean {
        val result = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)

        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)

        return bitmap
    }

    private inner class BitmapAsyncTask(val mBitmap: Bitmap):
        AsyncTask<Any, Void, String>()
    {
        private lateinit var mProgressDialog: Dialog

        override fun onPreExecute() {
            showProgressDialog()
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: Any?): String {
            var result = ""

            if (mBitmap != null) {
                try {

                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

                    val file = File(externalCacheDir?.absoluteFile.toString() +
                            File.separator + "KidzDrawing_" +
                            System.currentTimeMillis()/1000 + ".png")

                    val fileOutputStream = FileOutputStream(file)
                    fileOutputStream.write(bytes.toByteArray())
                    fileOutputStream.close()

                    result = file.absolutePath

                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }

            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result!!.isNotEmpty()) {
                Toast.makeText(this@MainActivity, "File created: $result",
                    Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "File not saved",
                    Toast.LENGTH_SHORT).show()
            }
            hideProgressDialog()

            // Share the doodle
            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result),
                null) { path, uri ->
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                shareIntent.type = "image/png"
                startActivity(Intent.createChooser(shareIntent, "Share"))
            }
        }

        private fun showProgressDialog() {
            mProgressDialog = Dialog(this@MainActivity)
            mProgressDialog.setContentView(R.layout.dialog_custom_progress)
            mProgressDialog.show()
        }

        private fun hideProgressDialog() {
            if (mProgressDialog.isShowing) {
                mProgressDialog.dismiss()
            }
        }

    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
        private const val TAG = "MainActivity"
    }
}
