package com.example.kidsdrawinggfg

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.media.MediaScannerConnection
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Gallery
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

//import com.example.kidsdrawinggfg.PaintView.Companion.colorList
//import com.example.kidsdrawinggfg.PaintView.Companion.currentBrush
//import com.example.kidsdrawinggfg.PaintView.Companion.pathList

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    //In Kotlin, a companion object is a construct that allows you to
    // define properties and methods within a class that can be accessed
    // without creating an instance of that class. It is similar to static
    // members in other programming languages.

//    companion object{
//        var path = Path()
//        var paintBrush = Paint()
//    }

    private lateinit var paintView: PaintView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        paintView = findViewById(R.id.paintView)

        val redBtn = findViewById<ImageButton>(R.id.redColor)

        val blueBtn = findViewById<ImageButton>(R.id.blueColor)

        val blackBtn = findViewById<ImageButton>(R.id.blackColor)

        val eraser = findViewById<ImageButton>(R.id.whiteColor)

        val ib_brush = findViewById<ImageButton>(R.id.ib_brush)

        val ib_gallery = findViewById<ImageButton>(R.id.ib_gallery)

        val ib_undo = findViewById<ImageButton>(R.id.ib_undo)

        val ib_save = findViewById<ImageButton>(R.id.ib_save)


        redBtn.setOnClickListener{
            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
            paintView.setCurrentBrushColor(Color.RED)
        }
        blueBtn.setOnClickListener{
            paintView.setCurrentBrushColor(Color.BLUE)
            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
        }
        blackBtn.setOnClickListener{
            paintView.setCurrentBrushColor(Color.BLACK)
            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
        }
        eraser.setOnClickListener{
            paintView.setCurrentBrushColor(Color.WHITE)
            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
        }

        ib_brush.setOnClickListener{
            showBrushSizeChooserDialog()
        }

        ib_gallery.setOnClickListener{
            //Very firstly we will check the app required a storage permission.
            // So we will add a permission in the Android.xml for storage.

            //First checking if the app is already having the permission

            if(isReadStorageAllowed()){
                // TODO(Step 20.1 - Selecting image from gallery if the permission is granted.)
                // This is for selecting the image from local store or let say from Gallery/Photos.

                val pickPhoto = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(pickPhoto, GALLERY)
            }
            else{
                requestStoragePermission()
            }
        }

        ib_undo.setOnClickListener{
            paintView.onCLickUndo()
        }

        ib_save.setOnClickListener {
            //First checking if app is already having the permission
            if(isReadStorageAllowed()){
                BitmapAsyncTask(getBitmapFromView(paintView)).execute()
            }else{

                //IF the app dont't have storage permission as will ask for it.
                requestStoragePermission()
            }
        }
    }

    private fun showBrushSizeChooserDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size :")

        val smallBtn = brushDialog.findViewById<ImageButton>(R.id.ib_small_brush)

        smallBtn.setOnClickListener {
            //val paintView: PaintView = brushDialog.findViewById(R.id.paintView)
            paintView.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }

        val mediumBtn = brushDialog.findViewById<ImageButton>(R.id.ib_medium_brush)
        mediumBtn.setOnClickListener {
           // val paintView: PaintView = brushDialog.findViewById(R.id.paintView)
            paintView.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }

        val largeBtn = brushDialog.findViewById<ImageButton>(R.id.ib_large_brush)
        largeBtn.setOnClickListener {
           // val paintView: PaintView = brushDialog.findViewById(R.id.paintView)
            paintView.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

    // TODO(Step 20.3 - An override method is called when the image is selected and we can identify the image using the unique code.)
    // START
    /**
     * This is override method here we get the selected image
     * based on the code what we have passed for selecting the image.
     */

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if(requestCode == GALLERY){
                try {
                    if(data!!.data != null){
                        //Here if the user selects the image from local storage make the image view visible.
                        // By default we will make it VISIBILITY as GONE.
                        var iv_background = findViewById<ImageView>(R.id.iv_background)
                        iv_background.visibility = View.VISIBLE

                        //Set the selected image to background view.
                        iv_background.setImageURI(data.data)
                    }else {
                        // If the selected image is not valid. Or not selected.
                        Toast.makeText(
                            this@MainActivity,
                            "Error in parsing the image or its corrupted.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: java.lang.Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    private fun requestStoragePermission(){

        /**
         * Gets whether you should show UI with rationale for requesting a permission.
         * You should do this only if you do not have the permission and the context in
         * which the permission is requested does not clearly communicate to the user
         * what would be the benefit from granting this permission.
         * <p>
         * For example, if you write a camera app, requesting the camera permission
         * would be expected by the user and no rationale for why it is requested is
         * needed. If however, the app needs location for tagging photos then a non-tech
         * savvy user may wonder how location is related to taking photos. In this case
         * you may choose to show UI with rationale of requesting this permission.
         * </p>
         *
         * @param activity The target activity.
         * @param permission A permission your app wants to request.
         * @return Whether you can show permission rationale UI.
         *
         */


        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())){
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission

            Toast.makeText(this, "Need permission to add a background", Toast.LENGTH_SHORT).show()
        }

        /**
         * Requests permissions to be granted to this application. These permissions
         * must be requested in your manifest, otherwise they will not be granted to your app.
         */

        //And finally ask for the permission

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this@MainActivity, "Permission granted now you can read the storage", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@MainActivity, "Oops you just denied the permission", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //TODO(Step - After giving an permission in Manifest file check that is it allowed or not for selecting the image from your phone)
    //START
    /**
     * We are calling this method to check the permission status
     */


    private fun isReadStorageAllowed(): Boolean{
        //Getting the permission status
        // Here the checkSelfPermission is
        /**
         * Determine whether <em>you</em> have been granted a particular permission.
         *
         * @param permission The name of the permission being checked.
         *
         */

        val result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)


        /**
         *
         * @return {@link android.content.pm.PackageManager#PERMISSION_GRANTED} if you have the
         * permission, or {@link android.content.pm.PackageManager#PERMISSION_DENIED} if not.
         *
         */


        //If permission is granted returning true and If permission is not granted returning false
        return result == PackageManager.PERMISSION_GRANTED
    }

    // TODO(Step 23.2 : Getting and bitmap Exporting the image to your phone storage.)
    // START
    /**
     * Create bitmap from view and returns it
     */

    private fun getBitmapFromView(view: View): Bitmap{

        //Define a bitmap with same size as the view
        //CreateBitmap : returns a mutable bitmap with specified width and heights
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)

        // Get the view's background
        val bgDrawable = view.background
        if(bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        }else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        }
        //draw the view in the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }

    // TODO(Step 23.3 : An asyncTask class to save the image.)
    // START
    /**
     * “A nested class marked as inner can access the members of its outer class.
     * Inner classes carry a reference to an object of an outer class:”
     * source: https://kotlinlang.org/docs/reference/nested-classes.html
     *
     * This is the background class is used to save the edited image of user in form of bitmap to the local storage.
     *
     * For Background we have used the AsyncTask
     *
     * Asynctask : Creates a new asynchronous task. This constructor must be invoked on the UI thread.
     */

    @Suppress("DEPRECATION")
    private inner class BitmapAsyncTask(val mBitmap: Bitmap?) :
    AsyncTask<Any, Void, String>(){

        // TODO(Step 24.2 - Creating an variable for showing and hiding the progress dialog while saving image)
        // STARTS
        /**
         * This is a progress dialog instance which we will initialize later on.
         */
        //private lateinit var mProgressDialog: Dialog
        //END

        private var mDialog: ProgressDialog? = null


        override fun onPreExecute() {
            super.onPreExecute()

            //TODO(Step 24.4 - Showing progress dialog while saving image)
            showProgressDialog()
        }

        override fun doInBackground(vararg params: Any?): String {
            var result = ""

            if(mBitmap != null){
                try {
                    val bytes = ByteArrayOutputStream() //Create a new byte array output stream.
                    //The Buffer capacity is initially 32 bytes, though its size increases if necessary

                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

                    /**
                     * Write a compressed version of the bitmap to the specified outputstream.
                     * If this returns true, the bitmap can be reconstructed by passing a
                     * corresponding inputstream to BitmapFactory.decodeStream(). Note: not
                     * all Formats support all bitmap configs directly, so it is possible that
                     * the returned bitmap from BitmapFactory could be in a different bitdepth,
                     * and/or may have lost per-pixel alpha (e.g. JPEG only supports opaque
                     * pixels).
                     *
                     * @param format   The format of the compressed image
                     * @param quality  Hint to the compressor, 0-100. 0 meaning compress for
                     *                 small size, 100 meaning compress for max quality. Some
                     *                 formats, like PNG which is lossless, will ignore the
                     *                 quality setting
                     * @param stream   The outputstream to write the compressed data.
                     * @return true if successfully compressed to the specified stream.
                     */

                    val f = File(
                        externalCacheDir!!.absoluteFile.toString()
                    +File.separator + "KidDrawinggfg_" + System.currentTimeMillis() / 1000 + ".jpg"
                    )

                    // Here the Environment : Provides access to environment variables.
                    // getExternalStorageDirectory : returns the primary shared/external storage directory.
                    // absoluteFile : Returns the absolute form of this abstract pathname.
                    // File.separator : The system-dependent default name-separator character. This string contains a single character.

                    val fo =
                        FileOutputStream(f) //create a file output stream to write to the file represented by specified object.
                    fo.write(bytes.toByteArray()) //Write bytes from the specified byte array to this file output stream.
                    fo.close() //closes this file output stream and releases any system resources associated with this stream. this file output stream may no longer be used for writing bytes.
                    result = f.absolutePath // The file absolute path is returned as a result
                } catch (e:java.lang.Exception){
                    result = ""
                    e.printStackTrace()
                }
            }
            return result
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            // TODO(Step 24.5 - hiding the progress dialog after the image is saved successfully.)
            cancelProgressDialog()


            if (!result.isEmpty()) {
                Toast.makeText(
                    this@MainActivity,
                    "File saved successfully :$result",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Something went wrong while saving the file.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // TODO (Step 25.1 - Sharing the download image file)
            //Start

            /* MediaScannerConnection provides a way for application to pass a
            newly created or downloaded media file to media scanner service.
            The media scanner service will read metadata from the file and add
            the file to media content provider.
            The MediaScannerConnectionClient provides an interface for the
            media scanner service to return the Uri for a newly scanned file
            to client of the MediaScanner Connection class.

            scanFile is used to scan the file when the connection is established
            with MediaScanner.
             */

            MediaScannerConnection.scanFile(
                this@MainActivity, arrayOf(result),null
            ) {path, uri ->
                //THis is used for sharing the image after it has being stored in storage.
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(
                    Intent.EXTRA_STREAM,
                    uri
                ) // A content: URI holding a stream of data associated with the intent, used to supply the data being sent.
                shareIntent.type = "image/jpeg" //The MIME type of the data being handled by this intent.
                startActivity(
                    Intent.createChooser(
                        shareIntent,
                        "Share"
                    )
                    // Activity Action: Display an activity chooser,
                    // allowing the user to pick what they want to before proceeding.
                    // This can be used as an alternative to the standard activity picker
                    // that is displayed by the system when you try to start an activity with multiple possible matches,
                    // with these differences in behavior:

                )
            }
        }
        //TODO(step 24.3 - Creating an functions to show and hide progress dialog while saving image.)
        /**
         * This function is used to show the progress dialog with the title and message to user.
         */

        private fun showProgressDialog(){
//            mProgressDialog = Dialog(this@MainActivity)
//
//            /*Set the screen content from a layout resource.
//            the resource will be inflated, adding all top-level views to the screen.
//             */
//            mProgressDialog.setContentView(R.layout.dialog_custom_progress)
//
//            //Start the dialog and display on screen.
//            mProgressDialog.show()
            @Suppress("DEPRECATION")
            mDialog = ProgressDialog.show(
                this@MainActivity,
                "",
                "Saving your image..."
            )
        }

        /**
         * This function is used to dismiss the progress dialog if it is visible to user.
         */

        private fun cancelProgressDialog(){
            //mProgressDialog.dismiss()
            if (mDialog != null) {
                mDialog!!.dismiss()
                mDialog = null
            }
        }
    }



    //TODO(Step - A unique code for asking the storage permission is declared in Companion Object.)

    companion object{

        /**
         * Permission code that will be checked in the method onRequestPermissionsResult
         *
         * For more Detail visit : https://developer.android.com/training/permissions/requesting#kotlin
         */

        private const val STORAGE_PERMISSION_CODE = 1;

        // TODO(Step 20.2 - A unique code for selecting an image from Gallery or let's say phone storage.)
        // This is to identify the selection of image from Gallery.
        private const val GALLERY = 2
    }


//    private fun currentColor(color : Int){
//        currentBrush = color
//        path = Path()
//    }
}