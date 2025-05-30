package cgeo.geocaching.wherigo;

import org.junit.Test;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class WherigoUtilsTest {

    // URL with "details.aspx"
    @Test
    public void testGetWhereigoUrl() {
        final String description = "<p style=\"max-width:670px;\"><a href=\"http://www.wherigo.com/cartridge/details.aspx?CGUID=c4577c31-09e9-44f0-ae48-83737e57adbd\"><img class=\"InsideTable\"";
        assertThat(WherigoUtils.scanWherigoGuids(description)).containsExactly("c4577c31-09e9-44f0-ae48-83737e57adbd");
    }

    // from GC461KF (URL with "download.aspx")
    @Test
    public void testGetWhereigGoUrlDownload() {
        final String description = "<a href=\"http://www.wherigo.com/cartridge/download.aspx?CGUID=ec53c2bc-98dc-4d5f-bf9b-0709931d53bc\">Download Cartridge</a><br>";
        assertThat(WherigoUtils.scanWherigoGuids(description)).containsExactly("ec53c2bc-98dc-4d5f-bf9b-0709931d53bc");
    }

    @Test
    public void testGetWhereigoUrlMultipleURLs() {
        final String description = "Lade dir den <font color=\"#FF0000\"><strong><a href=\"http://www.wherigo.com/cartridge/details.aspx?CGUID=22e7ef35-d8b6-4d4b-b506-8d8b520316b3\" target=\"_blank\"><img src=\"http://surfstoff.de/wherigo/gifs/wherigo_logo_klein.gif\" width=\"14\" height=\"14\" border=\"0\" /></a> <a href=\"http://www.wherigo.com/cartridge/details.aspx?CGUID=8fc0fb5e-7310-4685-ad06-143edf873ab0\" target=\"_blank\">Wherigo</a>";
        assertThat(WherigoUtils.scanWherigoGuids(description)).containsExactly("22e7ef35-d8b6-4d4b-b506-8d8b520316b3", "8fc0fb5e-7310-4685-ad06-143edf873ab0");
    }

    // from GC7K2KZ
    @Test
    public void testGetWherIGoDownloadURLHttps() {
        final String description = "Da kannsch da die Cartridge oaladn:<br>\n<br>\n<a target=\"_blank\" href=\"https://www.wherigo.com/cartridge/details.aspx?CGUID=a482bfed-47f0-4b2c-a9f9-c1e3c4ef48c6\"><img src=\"https://imgproxy.geocaching.com/5e0c76c4b8cccbb11eecc36b322a9177d6820421?url=https%3A%2F%2Fwww.muggelfrei.at%2Fcaches%2Fi-heart-innsbruck%2Fcartridge.png\"></a><br>";
        assertThat(WherigoUtils.scanWherigoGuids(description)).containsExactly("a482bfed-47f0-4b2c-a9f9-c1e3c4ef48c6");
    }

    // from GC7WDB4
    @Test
    public void testGetWhereigoUrlSameURLTwice() {
        final String description = "<p><strong><a href=\"http://www.wherigo.com/cartridge/details.aspx?CGUID=bb9a7000-c59c-4822-9e10-d779c752345f\">http://www.wherigo.com/cartridge/download.aspx?CGUID=bb9a7000-c59c-4822-9e10-d779c752345f</a></strong></p>";
    }

}
