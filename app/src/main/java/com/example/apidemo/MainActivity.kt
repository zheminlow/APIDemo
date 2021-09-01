package com.example.apidemo

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.apidemo.api.RetrofitInstance
import com.example.apidemo.data.MessageRespond
import com.example.apidemo.data.StudentRespond
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var  btnGet: Button
    private lateinit var  btnAdd :Button
    private lateinit var  btnLoad :Button
    private lateinit var btnBrowse:Button

    private lateinit var img : ImageView
    private var imgUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnGet = findViewById(R.id.btnGet)
        btnAdd = findViewById(R.id.btnAdd)
        btnLoad = findViewById(R.id.btnLoad)
        btnBrowse = findViewById(R.id.btnBrowse)
        img = findViewById(R.id.imgProfile)

        btnBrowse.setOnClickListener(){
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"

            launchSomeActivity.launch(intent)
        }

        btnGet.setOnClickListener(){
            val call = RetrofitInstance.api.getAll()

            call.enqueue(object: Callback<List<StudentRespond>>{
                override fun onResponse(
                    call: Call<List<StudentRespond>>,
                    response: Response<List<StudentRespond>>
                ) {
                    val rs = response.body()

                    var strResult = ""
                    for(student: StudentRespond in rs!!){
                        strResult += "${student.id} ${student.name} \n"
                    }

                    val tvResult = findViewById<TextView>(R.id.tvResult)
                    tvResult.text = strResult
                }

                override fun onFailure(call: Call<List<StudentRespond>>, t: Throwable) {
                   Toast.makeText(applicationContext,t.message,Toast.LENGTH_LONG).show()
                }
            })
        }

        btnLoad.setOnClickListener(){
            val call = RetrofitInstance.api.getById("W002")

            call.enqueue(object: Callback<StudentRespond>{
                override fun onResponse(
                    call: Call<StudentRespond>,
                    response: Response<StudentRespond>
                ) {
                    val rs = response.body()

                    findViewById<TextView>(R.id.tfID).text = rs!!.id
                    findViewById<TextView>(R.id.tfName).text = rs!!.name
                    findViewById<TextView>(R.id.tfProgramme).text = rs!!.programme

                    Glide.with(img.context).load(rs.imgURL).into(img)
                }

                override fun onFailure(call: Call<StudentRespond>, t: Throwable) {
                    Toast.makeText(applicationContext,t.message,Toast.LENGTH_LONG).show()
                }
            })
        }

        btnAdd.setOnClickListener(){
            val id = findViewById<TextView>(R.id.tfID).text.toString()
            val name = findViewById<TextView>(R.id.tfName).text.toString()
            val programme = findViewById<TextView>(R.id.tfProgramme).text.toString()

            //convert image to base64 string
            val bitmap = (img.getDrawable() as BitmapDrawable).bitmap
            val byteArray: ByteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArray)
            val strImg: String = Base64.encodeToString(byteArray.toByteArray(), Base64.DEFAULT)

            val call = RetrofitInstance.api.add(id, name, programme, strImg)

            call.enqueue(object: Callback<MessageRespond>{
                override fun onResponse(
                    call: Call<MessageRespond>,
                    response: Response<MessageRespond>
                ) {
                    val rs = response.body()
                    Toast.makeText(applicationContext,rs!!.message,Toast.LENGTH_LONG).show()
                }

                override fun onFailure(call: Call<MessageRespond>, t: Throwable) {
                    Toast.makeText(applicationContext,t.message,Toast.LENGTH_LONG).show()
                }

            })
        }
    }

    var launchSomeActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data

            imgUri  = data?.data
            img.setImageURI(data?.data)
        }
    }

}