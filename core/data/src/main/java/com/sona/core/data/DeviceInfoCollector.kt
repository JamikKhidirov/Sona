package com.sona.core.data

import android.content.Context
import com.sona.core.common.coroutines.DispatcherProvider
import com.sona.core.common.di.ApplicationContext
import com.sona.core.data.alarms.AlarmInfo
import com.sona.core.data.alarms.AndroidAlarmsReader
import com.sona.core.data.apps.AndroidAppsReader
import com.sona.core.data.apps.AppsInfo
import com.sona.core.data.battery.AndroidBatteryReader
import com.sona.core.data.battery.BatteryInfo
import com.sona.core.data.bluetooth.AndroidBluetoothReader
import com.sona.core.data.bluetooth.BluetoothInfo
import com.sona.core.data.browser.AndroidBrowserReader
import com.sona.core.data.browser.BrowserInfo
import com.sona.core.data.clipboard.AndroidClipboardReader
import com.sona.core.data.clipboard.ClipboardInfo
import com.sona.core.data.contacts.AndroidContactsReader
import com.sona.core.data.contacts.ContactsInfo
import com.sona.core.data.device.AndroidDeviceReader
import com.sona.core.data.device.DeviceInfo
import com.sona.core.data.display.AndroidDisplayReader
import com.sona.core.data.display.DisplayInfo
import com.sona.core.data.documents.AndroidDocumentsReader
import com.sona.core.data.documents.DocumentsInfo
import com.sona.core.data.downloads.AndroidDownloadsReader
import com.sona.core.data.downloads.DownloadsInfo
import com.sona.core.data.gallery.AndroidGalleryReader
import com.sona.core.data.gallery.GalleryInfo
import com.sona.core.data.hardware.AndroidHardwareReader
import com.sona.core.data.hardware.HardwareInfo
import com.sona.core.data.location.AndroidLocationReader
import com.sona.core.data.location.LocationInfo
import com.sona.core.data.media.AndroidMediaReader
import com.sona.core.data.media.MediaInfo
import com.sona.core.data.network.AndroidNetworkReader
import com.sona.core.data.network.NetworkInfo
import com.sona.core.data.nfc.AndroidNfcReader
import com.sona.core.data.nfc.NfcInfo
import com.sona.core.data.notifications.AndroidNotificationsReader
import com.sona.core.data.notifications.NotificationsInfo
import com.sona.core.data.performance.AndroidPerformanceReader
import com.sona.core.data.performance.PerformanceInfo
import com.sona.core.data.permissions.AndroidPermissionsReader
import com.sona.core.data.permissions.PermissionsInfo
import com.sona.core.data.sensors.AndroidSensorsReader
import com.sona.core.data.sensors.SensorsInfo
import com.sona.core.data.sms.AndroidSmsReader
import com.sona.core.data.sms.SmsInfo
import com.sona.core.data.sound.AndroidSoundReader
import com.sona.core.data.sound.SoundInfo
import com.sona.core.data.storage.AndroidStorageReader
import com.sona.core.data.storage.StorageInfo
import com.sona.core.data.usage.AndroidUsageReader
import com.sona.core.data.usage.UsageInfo
import com.sona.core.data.usb.AndroidUsbReader
import com.sona.core.data.usb.UsbInfo
import com.sona.core.data.vpn.AndroidVpnReader
import com.sona.core.data.vpn.VpnInfo
import com.sona.core.data.wifi.AndroidWifiReader
import com.sona.core.data.wifi.WifiInfoSnapshot
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single entry point for every on-device information source. Each reader checks its own
 * runtime permission and degrades gracefully instead of throwing when access is missing.
 */
@Singleton
class DeviceInfoCollector @Inject constructor(
    @ApplicationContext context: Context,
    private val dispatchers: DispatcherProvider,
) {

    private val alarms = AndroidAlarmsReader(context)
    private val apps = AndroidAppsReader(context)
    private val battery = AndroidBatteryReader(context)
    private val bluetooth = AndroidBluetoothReader(context)
    private val browser = AndroidBrowserReader(context)
    private val clipboard = AndroidClipboardReader(context)
    private val contacts = AndroidContactsReader(context)
    private val device = AndroidDeviceReader(context)
    private val display = AndroidDisplayReader(context)
    private val documents = AndroidDocumentsReader(context)
    private val downloads = AndroidDownloadsReader(context)
    private val gallery = AndroidGalleryReader(context)
    private val hardware = AndroidHardwareReader(context)
    private val location = AndroidLocationReader(context)
    private val media = AndroidMediaReader(context)
    private val network = AndroidNetworkReader(context)
    private val nfc = AndroidNfcReader(context)
    private val notifications = AndroidNotificationsReader(context)
    private val performance = AndroidPerformanceReader(context)
    private val permissions = AndroidPermissionsReader(context)
    private val sensors = AndroidSensorsReader(context)
    private val sms = AndroidSmsReader(context)
    private val sound = AndroidSoundReader(context)
    private val storage = AndroidStorageReader(context)
    private val usage = AndroidUsageReader(context)
    private val usb = AndroidUsbReader(context)
    private val vpn = AndroidVpnReader(context)
    private val wifi = AndroidWifiReader(context)

    suspend fun alarms(): AlarmInfo = alarms.read()
    suspend fun apps(): AppsInfo = apps.read()
    suspend fun battery(): BatteryInfo = battery.read()
    suspend fun bluetooth(): BluetoothInfo = bluetooth.read()
    suspend fun browser(): BrowserInfo = browser.read()
    suspend fun clipboard(): ClipboardInfo = clipboard.read()
    suspend fun contacts(): ContactsInfo = contacts.read()
    suspend fun device(): DeviceInfo = device.read()
    suspend fun display(): DisplayInfo = display.read()
    suspend fun documents(): DocumentsInfo = documents.read()
    suspend fun downloads(): DownloadsInfo = downloads.read()
    suspend fun gallery(): GalleryInfo = gallery.read()
    suspend fun hardware(): HardwareInfo = hardware.read()
    suspend fun location(): LocationInfo = location.read()
    suspend fun media(): MediaInfo = media.read()
    suspend fun network(): NetworkInfo = network.read()
    suspend fun nfc(): NfcInfo = nfc.read()
    suspend fun notifications(): NotificationsInfo = notifications.read()
    suspend fun performance(): PerformanceInfo = performance.read()
    suspend fun permissions(): PermissionsInfo = permissions.read()
    suspend fun sensors(): SensorsInfo = sensors.read()
    suspend fun sms(): SmsInfo = sms.read()
    suspend fun sound(): SoundInfo = sound.read()
    suspend fun storage(): StorageInfo = storage.read()
    suspend fun usage(): UsageInfo = usage.read()
    suspend fun usb(): UsbInfo = usb.read()
    suspend fun vpn(): VpnInfo = vpn.read()
    suspend fun wifi(): WifiInfoSnapshot = wifi.read()

    /** Collects every source concurrently. Permission-gated sources return empty results. */
    suspend fun snapshot(): DeviceSnapshot = coroutineScope {
        val alarmsDeferred = async { alarms.read() }
        val appsDeferred = async { apps.read() }
        val batteryDeferred = async { battery.read() }
        val bluetoothDeferred = async { bluetooth.read() }
        val browserDeferred = async { browser.read() }
        val clipboardDeferred = async { clipboard.read() }
        val contactsDeferred = async { contacts.read() }
        val deviceDeferred = async { device.read() }
        val displayDeferred = async { display.read() }
        val documentsDeferred = async { documents.read() }
        val downloadsDeferred = async { downloads.read() }
        val galleryDeferred = async { gallery.read() }
        val hardwareDeferred = async { hardware.read() }
        val locationDeferred = async { location.read() }
        val mediaDeferred = async { media.read() }
        val networkDeferred = async { network.read() }
        val nfcDeferred = async { nfc.read() }
        val notificationsDeferred = async { notifications.read() }
        val performanceDeferred = async { performance.read() }
        val permissionsDeferred = async { permissions.read() }
        val sensorsDeferred = async { sensors.read() }
        val smsDeferred = async { sms.read() }
        val soundDeferred = async { sound.read() }
        val storageDeferred = async { storage.read() }
        val usageDeferred = async { usage.read() }
        val usbDeferred = async { usb.read() }
        val vpnDeferred = async { vpn.read() }
        val wifiDeferred = async { wifi.read() }

        DeviceSnapshot(
            alarms = alarmsDeferred.await(),
            apps = appsDeferred.await(),
            battery = batteryDeferred.await(),
            bluetooth = bluetoothDeferred.await(),
            browser = browserDeferred.await(),
            clipboard = clipboardDeferred.await(),
            contacts = contactsDeferred.await(),
            device = deviceDeferred.await(),
            display = displayDeferred.await(),
            documents = documentsDeferred.await(),
            downloads = downloadsDeferred.await(),
            gallery = galleryDeferred.await(),
            hardware = hardwareDeferred.await(),
            location = locationDeferred.await(),
            media = mediaDeferred.await(),
            network = networkDeferred.await(),
            nfc = nfcDeferred.await(),
            notifications = notificationsDeferred.await(),
            performance = performanceDeferred.await(),
            permissions = permissionsDeferred.await(),
            sensors = sensorsDeferred.await(),
            sms = smsDeferred.await(),
            sound = soundDeferred.await(),
            storage = storageDeferred.await(),
            usage = usageDeferred.await(),
            usb = usbDeferred.await(),
            vpn = vpnDeferred.await(),
            wifi = wifiDeferred.await(),
        )
    }
}

data class DeviceSnapshot(
    val alarms: AlarmInfo,
    val apps: AppsInfo,
    val battery: BatteryInfo,
    val bluetooth: BluetoothInfo,
    val browser: BrowserInfo,
    val clipboard: ClipboardInfo,
    val contacts: ContactsInfo,
    val device: DeviceInfo,
    val display: DisplayInfo,
    val documents: DocumentsInfo,
    val downloads: DownloadsInfo,
    val gallery: GalleryInfo,
    val hardware: HardwareInfo,
    val location: LocationInfo,
    val media: MediaInfo,
    val network: NetworkInfo,
    val nfc: NfcInfo,
    val notifications: NotificationsInfo,
    val performance: PerformanceInfo,
    val permissions: PermissionsInfo,
    val sensors: SensorsInfo,
    val sms: SmsInfo,
    val sound: SoundInfo,
    val storage: StorageInfo,
    val usage: UsageInfo,
    val usb: UsbInfo,
    val vpn: VpnInfo,
    val wifi: WifiInfoSnapshot,
)
