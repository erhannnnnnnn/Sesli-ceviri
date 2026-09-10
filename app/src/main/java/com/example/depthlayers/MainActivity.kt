package com.example.depthlayers

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DepthLayerScreen()
                }
            }
        }
    }
}

@Composable
fun DepthLayerScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var processor by remember { mutableStateOf<DepthLayerProcessor?>(null) }

    LaunchedEffect(Unit) {
        try {
            processor = DepthLayerProcessor(context)
        } catch (e: Exception) {
            Toast.makeText(context, "Model yüklenemedi: assets içinde model eksik olabilir.", Toast.LENGTH_LONG).show()
        }
    }

    var inputBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var outputLayers by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isPngFormat by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            inputBitmap = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = true
                }
            }
            outputLayers = emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Derinlik Katmanı Ayrıştırıcı", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { imagePicker.launch("image/*") }) {
            Text("Resim Seç / İçe Aktar")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Çıktı Formatı: ")
            RadioButton(selected = isPngFormat, onClick = { isPngFormat = true })
            Text("PNG (Şeffaf)")
            Spacer(modifier = Modifier.width(8.dp))
            RadioButton(selected = !isPngFormat, onClick = { isPngFormat = false })
            Text("JPEG (Beyaz Arkaplan)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        inputBitmap?.let { bmp ->
            Text("Seçilen Resim:")
            Image(bitmap = bmp.asImageBitmap(), contentDescription = "Orijinal Resim", modifier = Modifier.fillMaxWidth().height(220.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val activeProcessor = processor
                    if (activeProcessor == null) {
                        Toast.makeText(context, "Model başlatılamadı!", Toast.LENGTH_SHORT).show()
                    } else {
                        isLoading = true
                        scope.launch {
                            try {
                                outputLayers = activeProcessor.processImage(bmp, isPng = isPngFormat)
                            } catch (e: Exception) {
                                Toast.makeText(context, "İşlem Hatası: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Katmanlar Ayrıştırılıyor...")
                } else {
                    Text("5 Katmanı Elde Et")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (outputLayers.isNotEmpty()) {
            Text("Oluşturulan 5 Katman (1: En Yakın -> 5: En Uzak):", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(outputLayers) { index, layerBmp ->
                    Card(modifier = Modifier.width(200.dp)) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Katman ${index + 1}", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(6.dp))
                            Image(bitmap = layerBmp.asImageBitmap(), contentDescription = "Katman ${index + 1}", modifier = Modifier.fillMaxWidth().height(150.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { saveBitmapToGallery(context, layerBmp, index + 1, isPngFormat) }) {
                                Text("Galeriye Kaydet")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, layerIndex: Int, isPng: Boolean) {
    val ext = if (isPng) "png" else "jpg"
    val mimeType = if (isPng) "image/png" else "image/jpeg"
    val compressFormat = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
    val filename = "depth_layer_${layerIndex}_${System.currentTimeMillis()}.$ext"

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/DepthLayers")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    if (uri != null) {
        resolver.openOutputStream(uri)?.use { out -> bitmap.compress(compressFormat, 100, out) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }
        Toast.makeText(context, "Katman $layerIndex galeriye kaydedildi!", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "Kayıt başarısız oldu", Toast.LENGTH_SHORT).show()
    }
}
