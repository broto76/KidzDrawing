package com.broto.kidzdrawing

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private var mImageButtonCurrentPaint: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawing_view.setSizeForBrush(22.toFloat())

        mImageButtonCurrentPaint = ll_paint_colors[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this,
                R.drawable.pallet_pressed)
        )

        ib_brush.setOnClickListener{
            showBrushSizeChooserDialog()
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

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    private fun showBrushSizeChooserDialog() {
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

    fun paintClicked(view: View) {
        if (view != mImageButtonCurrentPaint) {
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            drawing_view.setColorForBrush(colorTag)
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this,
                    R.drawable.pallet_pressed)
            )

            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this,
                    R.drawable.pallet_normal)
            )
            mImageButtonCurrentPaint = imageButton
        }
    }

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
    companion object {
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }
}
