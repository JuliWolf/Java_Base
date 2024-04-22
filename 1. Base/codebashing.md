+ [1. JKU]()
+ [2. X5c and X5t]()
+ [3. JWT]()
+ [4. JWS methods]()

# 1. JKU

+ [1. What is jku]()
+ [2. What jku does]()
+ [3. How to set jku]()
+ [4. How jku validation works]()
+ [5. What is SSRF]()
+ [6. How to validate jku]()

## 1. What is jku
JSON web key URI header in JSON web Token

## 2. What jku does
Specify the URI where JWK (JSON Web Key) set can be found

## 3. How to set jku
```java
jwkSet = JWKSet.load(jwt.getHeader().getJWKURL().toURL());
JWK key = getKeyFromJWKSet(KeyType.RSA, jwt.getHeader().getKeyID());
```

## 4. How jku validation works
- verification function loads thw JWK set from URL specified in the jku parameter without proper validation
- The verification function selects the JWK with the kid value specified in th forged JWT's header which points to public key
- The server performs signature verification using public key
- The server considers the forged JWT valid and authenticate

## 5. What is SSRF
Server-side request forgery attacks

## 6. How to validate jku
1. Ensure that the URL from the jku parameter matches the known URL of the token issuer's JWK set
```java
String[] allowedUrls = {
  "https://id.coolnews.me/.well-known/jwk-set.json",
  "https://id.codebashing.com/.well-known/jwk-set.json"
};

URL jkuValue = jwt = jwt.getHeader().getJWKURL().toURL();
boolean isAllowed = Arrays.asList(allowedUrls).contains(jkuValue.toString());

if (!isAllowed) {
  // stop the varification and reject the token
}
```

2. Limit the check to the domain to address this failure. 
    This approach will mitigate the risk of unavailability when the JWK set os relocated to a different directory on the same domain
3. Check jku URL agains the issuer clain (iss) of the JWT token
```java
URL jkuValue = jwt = jwt.getHeader().getJWKURL().toURL();
String issuer = jwt.getJWTClainsSet().getIssuer();
boolean isAllowed = jkuValue.toString().equals(issuer);

if (!isAllowed) {
  // stop the varification and reject the token
}
```
# END ----------------- 1. JKU -----------------

# 2. X5c and X5t

+ [1. What is X5c and X5t]()
+ [2. Details about X5c]()
+ [3. Details about x5t]()
+ [4. x5c validation example]()
+ [5. x5t validation example]()

## 1. What is X5c and X5t
The X5c and X5t header parameters are part of the JSON Web signature (JWS) PFC and are used for signature verification

## 2. Details about X5c
Contains an array of X.509 certificates used for token validation

We use first certificate in the array for JWS verification and the subsequent certificated from the certificate chain for first certificate verification 

## 3. Details about x5t
Is a certificate's identifier, similar to a key ID
JWS RFC defines an x5t parameter containing the certificate's hash(sha1). 
Also a similar parameter called `x5t#S256` uses sha256
It's easier to find the needed certificate by its identifier - hash

## 4. x5c validation example
```java
SignedJWT jwt = (SignedJWT) JWTParset.parse(token); // (1)
byte[] key = jwt.getHeader().getX509CertChain.get(0).decode(); // (2)
X509Certificate cert = X509CertUtils.parse(key);

Algorithm algorithmRSA = Algorithm.RSA256(
  com.nimbusds.jose.jwk.RSAKey.parse(cert).toRSAPublicKey(),
  null
);

JWTVErifier verifier = JWT.require(algorithmRSA).build();
verifier.verify(token); // (3)
```
1. The app parses incoming JWT token
2. The app gets the first certificate from the x5c header parameter
3. The app verifies the signature of the provided token using the certificate from the x5c header parameter

## 5. x5t validation example
1. Generate public and private keys and an X509 certificate with the generated public key
2. Change the value of the x5c header parameter to the generated certificate and also changes the token's payload
3. Generate the new token with the new header and payload abd created JWS based on private key

```java
SignedJWT jwt = (SignedJWT) JWTParset.parse(token);
List<Base64> certificateChain = signedJWT.getHeader().getX509CertChain();

boolean isTrusted = verifyCErtificateChain(certificateChain); // certificate hain verification

if (isTrusted) {
  X509Certificate cert = X509CertUtils.parse(certificateChain.get(0).decode());
  Algorithm algorithmRSA = Algorithm.RSA256(
      com.nimbusds.jose.jwk.RSAKey.parse(cert).toRSAPublicKey(),
      null
  );
  JWTVErifier verifier = JWT.require(algorithmRSA).build();
  verifier.verify(token);
} else {
  ...
}
```
# END ----------------- 2. X5c and X5t -----------------

# 3. JWT

+ [1. What is JWT]()
+ [2. What contains JWT]()
+ [3. How JWT decoding]()
+ [4. Exchange parties]()
+ [5. What is JWK]()
+ [6. What is JWS]()
+ [7. What is JWE]()
+ [8. JWT registered claims]()

## 1. What is JWT
JSON Web Token - is an open standard `RFC 7519` that defined a compact and self-contained way for securely transmitting digitally signed, trusted, and verified information between parties as a JSON object
Helpful for stateless application
- Authorization - to control access to routes, services, and resources for authenticated users
- Information exchange - ensuring users that the sender are who they say they are and verifying that the content hasn't been tampered with

## 2. What contains JWT
- Header
- Payload
- signature

## 3. How JWT decoding
The issuer grants a token to the token holder after authentication
The token holder then sends the token and a resource request to the resource owner, who uses the token to verify, authenticate, and authorize the token holder by parsing the token's payload 

## 4. Exchange parties
1. Token issuer responsible for owner - the token verifier; and the token holder - the user
2. The issuer grants a token to the holder. Then the token holder send this token along with a request for a resource to the resource owner
3. The resource owner uses the token to verify, authenticate and authorize the token holder

## 5. What is JWK
 JSON Web Key is a standard `RFC 7517` for representing cryptographic keys or sets of keys in JSON format

## 6. What is JWS
JSON Web Signature `RFC 7515` - is a way to digitally sign JSON data, ensuring its integrity and authenticity and providing a compact and secure method of verifying that the token has not been tampered with.
verifies that the header and payload have not been tampered with

## 7. What is JWE
JSON Web Encryption `RFC 7516` which encrypts the token's content
When encrypted you can ensure that the information inside token remains confidential and unauthorized parties won't be able to access it

## 8. JWT registered claims
1. iat - issued at (when the JWT was issued)
2. nbf - not before (the time before which JWT must not be accepted)
3. exp - expiration date (the expiration time after which the JWT must not be accepted for processing)
4. iss - issuer (the principal that issued the JWT)
5. aud - audience (identifies the recipients for which the JWT is intended)

# END ----------------- 3. JWT -----------------

# 4. JWS methods

+ [1. How to use JWS]()
+ [2. What is HMAC]()
+ [3. What is Digital Signature]()
+ [4. HMAC vs Digital Signature]()
+ [5. Distributed systems use cases]()

## 1. How to use JWS
- HMAC - uses a single secret key to generate and verify and HMAC
- Digital Signature - require a pair of public and private keys

## 2. What is HMAC
Hash-Based Message Authentication Code
JWS is generated and verified using a single secret key
HMAC function combines the JWT header, payload, and the secret key to calculate an HMAC, which is then used as JWS

## 3. What is Digital Signature
Digital signature-based JWS are typically generated and verified using either the RSA or elliptic curve algorithms
The ides is hat one party generated a signature using a private key, and any other party can verify it using the public key
The private key should be kept secret so no one can generate a JWS except for the key owner

## 4. HMAC vs Digital Signature
- HMAC is appropriate for situations where the same subject generated and validated tokens, like centralized systems 
- Digital Signature is appropriate for distributed systems where one party generated tokens and another verifies them

## 5. Distributed systems use cases
systems are commonly used to handle large-scale applications and services, providing improved reliability, scalability, and fault tolerance
So they need a way to agree on the key beforehand
Ways:
- **Static key** - one key for all systems, But difficult to change
- **Key Id** - kid header parameter - store a set of keys on the verification side and choosing one every time based on the ID provided in the header
- **Key itself in JWK or x5c** - send key in JWT header.
  - verification part must check the key send in the JWT header against an allowed  list
  - the allowed list compared the just-used verification key with the trusted pre-contracted keys list
- **Key set in the jku** - 
  - The generating party published the public key set. 
  - The verification party retrieves this set using the URL specified in the jku arguments and uses the KEY ID to select a specific key from the set
  - The verification part should ensure the specified URL is included in an allowed list
- **Certificate chain and Hashes** - 
  - The verification party uses a public key im the certificate chain specified in the x5c parameter, ensuring that the certificate is valid and the certificate is issued to the generation party

# END ----------------- 4. JWS methods -----------------