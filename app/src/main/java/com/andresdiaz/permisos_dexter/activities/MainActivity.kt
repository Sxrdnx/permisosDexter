package com.andresdiaz.permisos_dexter.activities

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.andresdiaz.permisos_dexter.R
import com.andresdiaz.permisos_dexter.enums.PermissionStatusEnum
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.CompositePermissionListener
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener
import kotlinx.android.synthetic.main.activity_main.*

/*app commpact sirve para que algunos elementos como el toolbar y asi funcionen
* en versiones menores de la 25, como elcambio del color solo vale para verciones
* 25 para arriba es necesario eredar de activity */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setButtonClicks()
    }

    private fun setButtonClicks() {
        buttonCameraPermission.setOnClickListener{checkCameraPermissions()}
        buttonContacts.setOnClickListener{checkContactsPermissions()}
        buttonAudio.setOnClickListener{checkAudioPermissions()}
        buttonAll.setOnClickListener{checkAllPermissions()}
    }

    //private fun checkCameraPermissions() =setPermissionHandler(Manifest.permission.CAMERA,textViewCamera)
    //private fun checkCameraPermissions()=setCameraPermissionHandlerWithDialog()
    private fun checkCameraPermissions()=setCameraPermissionHandlerWithSnackbar()

    private fun checkContactsPermissions() =setPermissionHandler(Manifest.permission.READ_CONTACTS,textViewContacts)

    private fun checkAudioPermissions() =setPermissionHandler(Manifest.permission.RECORD_AUDIO,textViewAudio)

    private fun setPermissionHandler(permission: String , textview:TextView){
        val context=this
        Dexter.withContext(context)
            .withPermission(permission)
            .withListener(object : PermissionListener{
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {//permiso aceptado
                    setPermissionStatus(textview,PermissionStatusEnum.GRANTED)
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse) {//permiso denegado
                    if (p0.isPermanentlyDenied){
                        setPermissionStatus(textview,PermissionStatusEnum.PERMANENTLY_DENIED)
                    }else{
                        setPermissionStatus(textview, PermissionStatusEnum.DENIED)
                    }
                }
                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken
                ) {
                    p1.continuePermissionRequest()//permite volver a preguntar sobre el permiso
                }

            }).check()
    }

    private fun setPermissionStatus(textView:TextView,status: PermissionStatusEnum){
        when(status){
           PermissionStatusEnum.GRANTED->{
               textView.text=getString(R.string.permission_status_granted)
               textView.setTextColor(ContextCompat.getColor(this, R.color.colorPermissionStatusGranted))
           }
            PermissionStatusEnum.DENIED->{
                textView.text=getString(R.string.permission_status_denied)
                textView.setTextColor(ContextCompat.getColor(this, R.color.colorPermissionStatusDenied))
            }
            PermissionStatusEnum.PERMANENTLY_DENIED->{
                textView.text=getString(R.string.permission_status_denied_permanently)
                textView.setTextColor(ContextCompat.getColor(this, R.color.colorPermissionStatusPermanentlyDenied))
            }

        }

    }
    private fun checkAllPermissions(){
        Dexter.withContext(this).withPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECORD_AUDIO
        ).withListener(object :MultiplePermissionsListener{
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                report?.let {
                    for(permission in report.grantedPermissionResponses){
                        when(permission.permissionName){
                            Manifest.permission.CAMERA->setPermissionStatus(textViewCamera,PermissionStatusEnum.GRANTED)
                            Manifest.permission.READ_CONTACTS->setPermissionStatus(textViewContacts,PermissionStatusEnum.GRANTED)
                            Manifest.permission.RECORD_AUDIO->setPermissionStatus(textViewAudio,PermissionStatusEnum.GRANTED)

                        }
                    }
                    for (permission in report.deniedPermissionResponses){
                        when(permission.permissionName){
                            Manifest.permission.CAMERA->{
                                if (permission.isPermanentlyDenied){
                                    setPermissionStatus(textViewCamera,PermissionStatusEnum.PERMANENTLY_DENIED)
                                }else{
                                    setPermissionStatus(textViewCamera,PermissionStatusEnum.DENIED)
                                }
                            }
                            Manifest.permission.READ_CONTACTS->{
                                if(permission.isPermanentlyDenied){
                                    setPermissionStatus(textViewContacts,PermissionStatusEnum.PERMANENTLY_DENIED)
                                }else{
                                    setPermissionStatus(textViewContacts,PermissionStatusEnum.DENIED)
                                }
                            }
                            Manifest.permission.RECORD_AUDIO->{
                                if (permission.isPermanentlyDenied){
                                    setPermissionStatus(textViewAudio,PermissionStatusEnum.PERMANENTLY_DENIED)
                                }else{
                                    setPermissionStatus(textViewAudio,PermissionStatusEnum.DENIED)
                                }
                            }
                        }
                    }
                }
            }

            override fun onPermissionRationaleShouldBeShown(p0: MutableList<PermissionRequest>?,p1: PermissionToken?) {
                p1?.continuePermissionRequest()
            }

        }).check()

    }

    private fun setCameraPermissionHandlerWithDialog(){
        val dialogPermissionListener= DialogOnDeniedPermissionListener.Builder
            .withContext(this)
            .withTitle("Camera Permission!")
            .withMessage("Camera permission is need to take pictures")
            .withButtonText(android.R.string.ok)
            .withIcon(R.mipmap.ic_launcher)
            .build()
        val permission=object : PermissionListener{
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
               setPermissionStatus(textViewCamera,PermissionStatusEnum.GRANTED)
            }
            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                if(p0!!.isPermanentlyDenied){
                    setPermissionStatus(textViewCamera,PermissionStatusEnum.PERMANENTLY_DENIED)
                }else{
                    setPermissionStatus(textViewCamera,PermissionStatusEnum.DENIED)
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?,
                p1: PermissionToken?
            ) {
                p1?.continuePermissionRequest()
            }
        }
        val composite=CompositePermissionListener(permission,dialogPermissionListener)
        Dexter.withContext(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(composite)
            .check()
    }
    private fun setCameraPermissionHandlerWithSnackbar(){
        val snackbarPermissionListener=SnackbarOnDeniedPermissionListener.Builder
            .with(root,"CAmera is need to take pictures")
            .withOpenSettingsButton("Settings")
            .withCallback(object: Snackbar.Callback(){
                override fun onShown(sb: Snackbar?) {
                    //manejador de enventos cuando el snack bar es visible
                }

                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    //para cuando el snack bar a sido borrado
                }
            }).build()

        val permission=object : PermissionListener{
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                setPermissionStatus(textViewCamera,PermissionStatusEnum.GRANTED)
            }
            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                if(p0!!.isPermanentlyDenied){
                    setPermissionStatus(textViewCamera,PermissionStatusEnum.PERMANENTLY_DENIED)
                }else{
                    setPermissionStatus(textViewCamera,PermissionStatusEnum.DENIED)
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?,
                p1: PermissionToken?
            ) {
                p1?.continuePermissionRequest()
            }
        }

        val composite=CompositePermissionListener(permission,snackbarPermissionListener)
        Dexter.withContext(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(composite)
            .check()
    }
}
