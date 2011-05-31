package nl.nikhef.jgridstart.passwordcache;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Security;
import java.security.cert.X509Certificate;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import junit.framework.TestCase;

public class PEMReaderTest extends TestCase {

    // we need BouncyCastle as a provider
    static {
	if (Security.getProvider("BC") == null)
	    Security.addProvider(new BouncyCastleProvider());
    }
    
    /** Write text to temporary file */
    protected static File writeTemp(String id, String suffix, String contents) throws IOException {
	File f = File.createTempFile("pemreadertest-"+id, suffix);
	f.deleteOnExit();
	FileWriter w = new FileWriter(f);
	w.append(contents);
	w.close();
	return f;
    }
    
    /** Test reading of single certificate */ 
    @Test
    public void testCert() throws Exception {
	File pem = writeTemp("cert", "pem", cert1);
	Object _cert = PEMReader.readObject(pem);
	assertTrue(_cert instanceof X509Certificate);
	X509Certificate cert = (X509Certificate)_cert;
	assertEquals(cert1DN, cert.getSubjectDN().toString());
    }
    
    /** Test reading of certificate chain (first object) */
    @Test
    public void testCertChain1() throws Exception {
	File pem = writeTemp("certchain1", "pem", cert1+"\n"+cert2);
	Object _cert = PEMReader.readObject(pem);
	assertTrue(_cert instanceof X509Certificate);
	X509Certificate cert = (X509Certificate)_cert;
	assertEquals(cert1DN, cert.getSubjectDN().toString());
    }    

    /** Test reading of certificate chain */
    @Test
    public void testCertChain2() throws Exception {
	File pem = writeTemp("certchain2", "pem", cert1+"\n"+cert2);
	PEMReader r = new PEMReader(pem);
	Object _cert1 = r.readObject();
	assertTrue(_cert1 instanceof X509Certificate);
	X509Certificate cert1 = (X509Certificate)_cert1;
	assertEquals(cert1DN, cert1.getSubjectDN().toString());
	Object _cert2 = r.readObject();
	assertTrue(_cert2 instanceof X509Certificate);
	X509Certificate cert2 = (X509Certificate)_cert2;
	assertEquals(cert2DN, cert2.getSubjectDN().toString());
	assertEquals(null, r.readObject());
    }
    
    /** Test reading of single certificate with garbage (pre) */ 
    @Test
    public void testCertGarbagePre() throws Exception {
	File pem = writeTemp("cert-garbage-pre", "pem", "Hi there\n"+cert1);
	Object _cert = PEMReader.readObject(pem);
	assertTrue(_cert instanceof X509Certificate);
	X509Certificate cert = (X509Certificate)_cert;
	assertEquals(cert1DN, cert.getSubjectDN().toString());
    }

    /** Test reading of single certificate with garbage (post) */ 
    @Test
    public void testCertGarbagePost() throws Exception {
	File pem = writeTemp("cert-garbagepost", "pem", cert1+"Blah blah blah\n");
	Object _cert = PEMReader.readObject(pem);
	assertTrue(_cert instanceof X509Certificate);
	X509Certificate cert = (X509Certificate)_cert;
	assertEquals(cert1DN, cert.getSubjectDN().toString());
    }
    
    /** Test reading of certificate chain with garbage in between*/
    @Test
    public void testCertChainGarbageMiddle() throws Exception {
	File pem = writeTemp("certchain2-garbage-middle", "pem", cert1+"Jolly ho\n"+cert2);
	PEMReader r = new PEMReader(pem);
	Object _cert1 = r.readObject();
	assertTrue(_cert1 instanceof X509Certificate);
	X509Certificate cert1 = (X509Certificate)_cert1;
	assertEquals(cert1DN, cert1.getSubjectDN().toString());
	Object _cert2 = r.readObject();
	assertTrue(_cert2 instanceof X509Certificate);
	X509Certificate cert2 = (X509Certificate)_cert2;
	assertEquals(cert2DN, cert2.getSubjectDN().toString());
	assertEquals(null, r.readObject());
    }
    
    /* Data */
    
    // CAcert certificate
    protected static String cert1DN = "O=Root CA,OU=http://www.cacert.org,CN=CA Cert Signing Authority,E=support@cacert.org";
    protected static String cert1 = 
	"-----BEGIN CERTIFICATE-----\n" +
	"MIIHPTCCBSWgAwIBAgIBADANBgkqhkiG9w0BAQQFADB5MRAwDgYDVQQKEwdSb290\n" +
	"IENBMR4wHAYDVQQLExVodHRwOi8vd3d3LmNhY2VydC5vcmcxIjAgBgNVBAMTGUNB\n" +
	"IENlcnQgU2lnbmluZyBBdXRob3JpdHkxITAfBgkqhkiG9w0BCQEWEnN1cHBvcnRA\n" +
	"Y2FjZXJ0Lm9yZzAeFw0wMzAzMzAxMjI5NDlaFw0zMzAzMjkxMjI5NDlaMHkxEDAO\n" +
	"BgNVBAoTB1Jvb3QgQ0ExHjAcBgNVBAsTFWh0dHA6Ly93d3cuY2FjZXJ0Lm9yZzEi\n" +
	"MCAGA1UEAxMZQ0EgQ2VydCBTaWduaW5nIEF1dGhvcml0eTEhMB8GCSqGSIb3DQEJ\n" +
	"ARYSc3VwcG9ydEBjYWNlcnQub3JnMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIIC\n" +
	"CgKCAgEAziLA4kZ97DYoB1CW8qAzQIxL8TtmPzHlawI229Z89vGIj053NgVBlfkJ\n" +
	"8BLPRoZzYLdufujAWGSuzbCtRRcMY/pnCujW0r8+55jE8Ez64AO7NV1sId6eINm6\n" +
	"zWYyN3L69wj1x81YyY7nDl7qPv4coRQKFWyGhFtkZip6qUtTefWIonvuLwphK42y\n" +
	"fk1WpRPs6tqSnqxEQR5YYGUFZvjARL3LlPdCfgv3ZWiYUQXw8wWRBB0bF4LsyFe7\n" +
	"w2t6iPGwcswlWyCR7BYCEo8y6RcYSNDHBS4CMEK4JZwFaz+qOqfrU0j36NK2B5jc\n" +
	"G8Y0f3/JHIJ6BVgrCFvzOKKrF11myZjXnhCLotLddJr3cQxyYN/Nb5gznZY0dj4k\n" +
	"epKwDpUeb+agRThHqtdB7Uq3EvbXG4OKDy7YCbZZ16oE/9KTfWgu3YtLq1i6L43q\n" +
	"laegw1SJpfvbi1EinbLDvhG+LJGGi5Z4rSDTii8aP8bQUWWHIbEZAWV/RRyH9XzQ\n" +
	"QUxPKZgh/TMfdQwEUfoZd9vUFBzugcMd9Zi3aQaRIt0AUMyBMawSB3s42mhb5ivU\n" +
	"fslfrejrckzzAeVLIL+aplfKkQABi6F1ITe1Yw1nPkZPcCBnzsXWWdsC4PDSy826\n" +
	"YreQQejdIOQpvGQpQsgi3Hia/0PsmBsJUUtaWsJx8cTLc6nloQsCAwEAAaOCAc4w\n" +
	"ggHKMB0GA1UdDgQWBBQWtTIb1Mfz4OaO873SsDrusjkY0TCBowYDVR0jBIGbMIGY\n" +
	"gBQWtTIb1Mfz4OaO873SsDrusjkY0aF9pHsweTEQMA4GA1UEChMHUm9vdCBDQTEe\n" +
	"MBwGA1UECxMVaHR0cDovL3d3dy5jYWNlcnQub3JnMSIwIAYDVQQDExlDQSBDZXJ0\n" +
	"IFNpZ25pbmcgQXV0aG9yaXR5MSEwHwYJKoZIhvcNAQkBFhJzdXBwb3J0QGNhY2Vy\n" +
	"dC5vcmeCAQAwDwYDVR0TAQH/BAUwAwEB/zAyBgNVHR8EKzApMCegJaAjhiFodHRw\n" +
	"czovL3d3dy5jYWNlcnQub3JnL3Jldm9rZS5jcmwwMAYJYIZIAYb4QgEEBCMWIWh0\n" +
	"dHBzOi8vd3d3LmNhY2VydC5vcmcvcmV2b2tlLmNybDA0BglghkgBhvhCAQgEJxYl\n" +
	"aHR0cDovL3d3dy5jYWNlcnQub3JnL2luZGV4LnBocD9pZD0xMDBWBglghkgBhvhC\n" +
	"AQ0ESRZHVG8gZ2V0IHlvdXIgb3duIGNlcnRpZmljYXRlIGZvciBGUkVFIGhlYWQg\n" +
	"b3ZlciB0byBodHRwOi8vd3d3LmNhY2VydC5vcmcwDQYJKoZIhvcNAQEEBQADggIB\n" +
	"ACjH7pyCArpcgBLKNQodgW+JapnM8mgPf6fhjViVPr3yBsOQWqy1YPaZQwGjiHCc\n" +
	"nWKdpIevZ1gNMDY75q1I08t0AoZxPuIrA2jxNGJARjtT6ij0rPtmlVOKTV39O9lg\n" +
	"18p5aTuxZZKmxoGCXJzN600BiqXfEVWqFcofN8CCmHBh22p8lqOOLlQ+TyGpkO/c\n" +
	"gr/c6EWtTZBzCDyUZbAEmXZ/4rzCahWqlwQ3JNgelE5tDlG+1sSPypZt90Pf6DBl\n" +
	"Jzt7u0NDY8RD97LsaMzhGY4i+5jhe1o+ATc7iwiwovOVThrLm82asduycPAtStvY\n" +
	"sONvRUgzEv/+PDIqVPfE94rwiCPCR/5kenHA0R6mY7AHfqQv0wGP3J8rtsYIqQ+T\n" +
	"SCX8Ev2fQtzzxD72V7DX3WnRBnc0CkvSyqD/HMaMyRa+xMwyN2hzXwj7UfdJUzYF\n" +
	"CpUCTPJ5GhD22Dp1nPMd8aINcGeGG7MW9S/lpOt5hvk9C8JzC6WZrG/8Z7jlLwum\n" +
	"GCSNe9FINSkYQKyTYOGWhlC0elnYjyELn8+CkcY7v2vcB5G5l1YjqrZslMZIBjzk\n" +
	"zk6q5PYvCdxTby78dOs6Y5nCpqyJvKeyRKANihDjbPIky/qbn3BHLt4Ui9SyIAmW\n" +
	"omTxJBzcoTWcFbLUvFUufQb1nA5V9FrWk9p2rSVzTMVD\n" +
	"-----END CERTIFICATE-----";
    
    // CAcert chain
    protected static String cert2DN = "O=CAcert Inc.,OU=http://www.CAcert.org,CN=CAcert Class 3 Root";
    protected static String cert2 =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIIGCDCCA/CgAwIBAgIBATANBgkqhkiG9w0BAQQFADB5MRAwDgYDVQQKEwdSb290\n" +
        "IENBMR4wHAYDVQQLExVodHRwOi8vd3d3LmNhY2VydC5vcmcxIjAgBgNVBAMTGUNB\n" +
        "IENlcnQgU2lnbmluZyBBdXRob3JpdHkxITAfBgkqhkiG9w0BCQEWEnN1cHBvcnRA\n" +
        "Y2FjZXJ0Lm9yZzAeFw0wNTEwMTQwNzM2NTVaFw0zMzAzMjgwNzM2NTVaMFQxFDAS\n" +
        "BgNVBAoTC0NBY2VydCBJbmMuMR4wHAYDVQQLExVodHRwOi8vd3d3LkNBY2VydC5v\n" +
        "cmcxHDAaBgNVBAMTE0NBY2VydCBDbGFzcyAzIFJvb3QwggIiMA0GCSqGSIb3DQEB\n" +
        "AQUAA4ICDwAwggIKAoICAQCrSTURSHzSJn5TlM9Dqd0o10Iqi/OHeBlYfA+e2ol9\n" +
        "4fvrcpANdKGWZKufoCSZc9riVXbHF3v1BKxGuMO+f2SNEGwk82GcwPKQ+lHm9WkB\n" +
        "Y8MPVuJKQs/iRIwlKKjFeQl9RrmK8+nzNCkIReQcn8uUBByBqBSzmGXEQ+xOgo0J\n" +
        "0b2qW42S0OzekMV/CsLj6+YxWl50PpczWejDAz1gM7/30W9HxM3uYoNSbi4ImqTZ\n" +
        "FRiRpoWSR7CuSOtttyHshRpocjWr//AQXcD0lKdq1TuSfkyQBX6TwSyLpI5idBVx\n" +
        "bgtxA+qvFTia1NIFcm+M+SvrWnIl+TlG43IbPgTDZCciECqKT1inA62+tC4T7V2q\n" +
        "SNfVfdQqe1z6RgRQ5MwOQluM7dvyz/yWk+DbETZUYjQ4jwxgmzuXVjit89Jbi6Bb\n" +
        "6k6WuHzX1aCGcEDTkSm3ojyt9Yy7zxqSiuQ0e8DYbF/pCsLDpyCaWt8sXVJcukfV\n" +
        "m+8kKHA4IC/VfynAskEDaJLM4JzMl0tF7zoQCqtwOpiVcK01seqFK6QcgCExqa5g\n" +
        "eoAmSAC4AcCTY1UikTxW56/bOiXzjzFU6iaLgVn5odFTEcV7nQP2dBHgbbEsPyyG\n" +
        "kZlxmqZ3izRg0RS0LKydr4wQ05/EavhvE/xzWfdmQnQeiuP43NJvmJzLR5iVQAX7\n" +
        "6QIDAQABo4G/MIG8MA8GA1UdEwEB/wQFMAMBAf8wXQYIKwYBBQUHAQEEUTBPMCMG\n" +
        "CCsGAQUFBzABhhdodHRwOi8vb2NzcC5DQWNlcnQub3JnLzAoBggrBgEFBQcwAoYc\n" +
        "aHR0cDovL3d3dy5DQWNlcnQub3JnL2NhLmNydDBKBgNVHSAEQzBBMD8GCCsGAQQB\n" +
        "gZBKMDMwMQYIKwYBBQUHAgEWJWh0dHA6Ly93d3cuQ0FjZXJ0Lm9yZy9pbmRleC5w\n" +
        "aHA/aWQ9MTAwDQYJKoZIhvcNAQEEBQADggIBAH8IiKHaGlBJ2on7oQhy84r3HsQ6\n" +
        "tHlbIDCxRd7CXdNlafHCXVRUPIVfuXtCkcKZ/RtRm6tGpaEQU55tiKxzbiwzpvD0\n" +
        "nuB1wT6IRanhZkP+VlrRekF490DaSjrxC1uluxYG5sLnk7mFTZdPsR44Q4Dvmw2M\n" +
        "77inYACHV30eRBzLI++bPJmdr7UpHEV5FpZNJ23xHGzDwlVks7wU4vOkHx4y/CcV\n" +
        "Bc/dLq4+gmF78CEQGPZE6lM5+dzQmiDgxrvgu1pPxJnIB721vaLbLmINQjRBvP+L\n" +
        "ivVRIqqIMADisNS8vmW61QNXeZvo3MhN+FDtkaVSKKKs+zZYPumUK5FQhxvWXtaM\n" +
        "zPcPEAxSTtAWYeXlCmy/F8dyRlecmPVsYGN6b165Ti/Iubm7aoW8mA3t+T6XhDSU\n" +
        "rgCvoeXnkm5OvfPi2RSLXNLrAWygF6UtEOucekq9ve7O/e0iQKtwOIj1CodqwqsF\n" +
        "YMlIBdpTwd5Ed2qz8zw87YC8pjhKKSRf/lk7myV6VmMAZLldpGJ9VzZPrYPvH5JT\n" +
        "oI53V93lYRE9IwCQTDz6o2CTBKOvNfYOao9PSmCnhQVsRqGP9Md246FZV/dxssRu\n" +
        "FFxtbUFm3xuTsdQAw+7Lzzw9IYCpX2Nl/N3gX6T0K/CFcUHUZyX7GrGXrtaZghNB\n" +
        "0m6lG5kngOcLqagA\n" +
        "-----END CERTIFICATE-----\n"; 
}
