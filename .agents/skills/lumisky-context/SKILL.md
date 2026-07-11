---
name: lumisky-context
description: Lumisky Engine geliştirmeleri sırasında token tasarrufu ve modüler bağlam optimizasyonunu sağlamak için uyulması gereken yönergeler.
---

# Lumisky Context & Modularity Skill

Bu beceri dosyası, Lumisky projesindeki (v4 / v5) geliştirme ve hata ayıklama süreçlerinin, token havuzunu aşırı tüketmeden (bağlam optimizasyonu ile) yürütülmesi için gereken spesifik kuralları barındırır. Sadece Lumisky ile ilgili ağır yük gerektiren iş akışlarında referans alınmalıdır.

## 1. Mimari ve Kod Navigasyonu
- **Modüler Okuma:** Lumisky geniş bir yapıya sahip olabilir. Kod tabanını anlamak için bütün bir dizini okumak yerine `grep_search` aracıyla spesifik metotları/sınıfları aratın ve sadece bulundukları dosyaları okuyun.
- **Hedef Odaklı Değişiklik:** Büyük dosyalarda yapılacak güncellemeleri, tüm dosyayı tekrar yazdırmak yerine sadece değiştirilen satırları (`multi_replace_file_content` veya `replace_file_content` ile) hedef alarak yapın.

## 2. Dış Veri (MCP) ve Komut Çıktıları
- **Log ve Çıktı Yönetimi:** Büyük log dosyalarını veya test raporlarını tümüyle okutmak yerine `tail -n 50` gibi yapılandırılmış araçlarla veya arama yetenekleriyle daraltılmış olarak inceleyin.
- **MCP Kullanımı:** Dış sunuculardan (veritabanı, tasarım dokümanları, github vb.) veri çekerken MCP araçlarını (`call_mcp_tool`) kullanarak yalnızca ihtiyaç duyulan tablonun, sayfanın veya commit'in verilerini bağlama dâhil edin.

## 3. Bağlamın Korunması
- **Büyük Dosya Kuralı:** Eğer incelenen dosya 300 satırdan uzunsa, modelin belleğini şişirmemek için `StartLine` ve `EndLine` özellikleriyle dosyayı parçalar halinde inceleyin.
- **Özetleme:** Görev tamamlandıktan sonra, gelecekteki adımlar için bağlamı tekrarlamak yerine iş planını `walkthrough.md` veya `task.md` içine kısa notlar olarak kaydedin.
