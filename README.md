# PrepFlow — İngilizce Hazırlık Asistanı

Tamamen çevrimdışı (offline-first), Kotlin + Jetpack Compose + Room + Jetpack Glance ile geliştirilmiş modern Android uygulaması.

## Özellikler
- **Bugün (Dashboard):** Günün dersleri (şu an işlenen/sıradaki vurgulu), yaklaşan sınav geri sayımı, tek dokunuşla tamamlanan acil ödevler ve kaynak özeti.
- **Ödevler:** Liste ve aylık Takvim görünümü, beceri/öncelik/durum filtreleri, alt görev checklist'i ve çoklu etiketli bağlantı yönlendirme.
- **Ders Programı:** Pazartesi–Cuma (isteğe bağlı hafta sonu) haftalık ders programı.
- **Akademik:** Otomatik ağırlıklı not ortalaması hesaplayan Sınavlar ve hızlı `-/+` kontrollü Kaynak İlerleme takibi.
- **Ana Ekran Widget'ı:** Jetpack Glance ile koyu Material You + cam tasarım diline uyumlu, veritabanı değiştikçe otomatik yenilenen widget.

## GitHub Actions ile Otomatik APK Derleme
1. Bu klasörü yeni bir GitHub deposuna yükleyin (`main` veya `master` dalına).
2. GitHub deposundaki **Actions** sekmesinden `Build PrepFlow Android APK` iş akışının tamamlanmasını bekleyin.
3. İş akışı sayfasının altındaki **Artifacts → PrepFlow-debug-apk** bölümünden `.apk` dosyasını indirip Android cihazınıza kurun.
