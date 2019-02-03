package com.example.mad9042_week2

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Context.CAMERA_SERVICE
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.EditText
import kotlin.math.max


class MainActivity : AppCompatActivity() {

    private lateinit var mSensorManager: SensorManager
    private var mSensor: Sensor? = null

    var maxLight = 0.0f
    var ambientLight = 0.0f

    var originalAngle=0.0f
    var originalAngleFlag=true

    var x = 0.0f
    var y = 0.0f
    var z = 0.0f

    var flashLightStatus = false
    var deviceHasCameraFlash: Boolean = false


    lateinit var camManager : CameraManager
    lateinit var cameraId : String

    lateinit var xEditText : EditText
    lateinit var yEditText : EditText
    lateinit var zEditText : EditText

    lateinit var screenBackground: View
    lateinit var vibrateMotor: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Get the screen background so that you can change its color
        screenBackground = findViewById(R.id.screenBackground)



        // flashlight code  example.
        camManager= getSystemService(Context.CAMERA_SERVICE)  as CameraManager
        cameraId = camManager.cameraIdList[0] // Usually front camera is at 0 position.
        deviceHasCameraFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);


        val flashLight = findViewById<Button>(R.id.flashlight_button)
        flashLight.setOnClickListener{
            try {
                if(deviceHasCameraFlash){
                    if(flashLightStatus){//when light on

                        //turn the light off:
                        camManager.setTorchMode(cameraId, false)
                        flashLightStatus = !flashLightStatus  //flip true to false, or false to true
                    }
                    //hold the phone tilted below the horizontal plane, and the ambient light is less than 100 lumens.
                    else{
                        Log.i("Message","the ambient light is greater than 100 lumens")
                    }
                }


            }
            catch( e:Throwable)
            {
                Log.i("Exception:", e.message)
            }
        }
        // end of flashlight example




        //Vibration example. Look at powerpoint slides on vibration
        val vibrateButton = findViewById<Button>(R.id.vibrate_button)
        vibrateMotor = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        vibrateButton.setOnClickListener{

            val pattern = longArrayOf(500, 500, 500, 500)
            val amplitudes = intArrayOf(0, 255, 0, 128)
            // api 26 or newer: vibrateMotor.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1) )

            vibrateMotor.vibrate(pattern, -1)
        }
        //end of vibration example



        //sensor example. Look at powerpoint slides on sensor readings.
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        //Now get a sensor:
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        mSensorManager.registerListener(
            OrientationListener(), // look at line 125 for class declaration
            mSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )


        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        mSensorManager.registerListener(
            AmbientLightListener(), // look at line 145 for class definition
            mSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        //load the edit text from the screen. We will write to them later.
        xEditText = findViewById(R.id.x_values)
        yEditText = findViewById(R.id.y_values)
        zEditText = findViewById(R.id.z_values)
    }

    var lastVibrate = 0L
    inner class OrientationListener : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val values = event.values

            x = values[0]
            y = values[1]
            z = values[2]

            //get original angle value
            if(originalAngleFlag){
                originalAngle=x
                originalAngleFlag=false
            }

            //turn more than 45 degrees away from the original angle, vibrate the phone
            val angleDif=Math.abs(x-originalAngle)
            var timeNow = System.currentTimeMillis()

            if(angleDif>45 && (timeNow - lastVibrate > 2000 )){
                val pattern = longArrayOf(500, 500, 500, 500)
                val amplitudes = intArrayOf(0, 255, 0, 128)
                // api 26 or newer: vibrateMotor.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1) )
                lastVibrate = timeNow
                vibrateMotor.vibrate(pattern, -1)
            }

            xEditText.setText("X: $x, origin angle: $originalAngle")
            yEditText.setText("Y: $y, ambient light: $ambientLight") //rotation around x, 0.0 when vertical, with positive values when the z-axis moves toward the y-axis.
            zEditText.setText("Z: $z, angle diff: $angleDif")

            //When the phone’s orientation sensor crosses 0 degrees towards you, the flashlight turns off.

                camManager.setTorchMode(cameraId, y>0&&ambientLight<100)


        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // Do something here if sensor accuracy changes.
            // You must implement this callback in your code.
        }
    }


    inner class AmbientLightListener : SensorEventListener
    {
        override fun onSensorChanged(event: SensorEvent) {
            val values = event.values
            Log.i("Light:", "Lux:"+ values[0])
            ambientLight=values[0]
            //camManager.setTorchMode(cameraId, y>0&&ambientLight<100)
            maxLight = Math.max(maxLight, values[0])
            val intensity = (values[0]*255.0/maxLight).toInt()
           //Api 26 or newer: screenBackground.setBackgroundColor(Color.rgb( 1.0f, values[0]/maxLight, values[0]/maxLight))

            //API 25 or lower:
            val color =  Color.argb(255,  255, intensity, intensity)
            screenBackground.setBackgroundColor(color)

        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // Do something here if sensor accuracy changes.
            // You must implement this callback in your code.
        }
    }

    //end of sensor example
}
