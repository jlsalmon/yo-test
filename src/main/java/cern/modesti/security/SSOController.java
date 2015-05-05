///*
// * Copyright 2014 Vincenzo De Notaris
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package cern.modesti.security;
//
//import org.opensaml.saml2.metadata.provider.MetadataProviderException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AnonymousAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.saml.metadata.MetadataManager;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.Set;
//
//@Controller
//@RequestMapping("/saml")
//public class SSOController {
//
//  // Logger
//  private static final Logger LOG = LoggerFactory.getLogger(SSOController.class);
//
//  @Autowired
//  private MetadataManager metadata;
//
//  @RequestMapping(value = "/idpSelection", method = RequestMethod.GET)
//  public String idpSelection(HttpServletRequest request) throws MetadataProviderException {
//    if (!(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)) {
//      LOG.warn("The current user is already logged.");
//      return "redirect:/landing";
//    } else {
//      if (isForwarded(request)) {
//        String idp = metadata.getDefaultIDP();
////        Set<String> idps = metadata.getIDPEntityNames();
////        for (String idp : idps)
//        LOG.info("Configured Identity Provider for SSO: " + idp);
////        model.addAttribute("idps", idps);
//        return "redirect:saml/login?idp=" + idp;
//      } else {
//        LOG.warn("Direct accesses to '/idpSelection' route are not allowed");
//        return "redirect:/";
//      }
//    }
//  }
//
//  /*
//   * Checks if an HTTP request is forwarded from servlet.
//   */
//  private boolean isForwarded(HttpServletRequest request) {
//    if (request.getAttribute("javax.servlet.forward.request_uri") == null) return false;
//    else return true;
//  }
//
//}
//
//// <?xml version="1.0" encoding="UTF-8"?><samlp:Response xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol" Consent="urn:oasis:names:tc:SAML:2.0:consent:unspecified" Destination="https://modesti-test.cern.ch:8443/saml/SSO" ID="_2cb50a2e-9285-4cff-895a-956c0cbed357" InResponseTo="a4jh103j9e74h3ai16e5cf7eb774117" IssueInstant="2015-05-05T15:51:11.314Z" Version="2.0"><Issuer xmlns="urn:oasis:names:tc:SAML:2.0:assertion">https://cern.ch/login</Issuer><samlp:Status><samlp:StatusCode Value="urn:oasis:names:tc:SAML:2.0:status:Success"/></samlp:Status><EncryptedAssertion xmlns="urn:oasis:names:tc:SAML:2.0:assertion"><xenc:EncryptedData xmlns:xenc="http://www.w3.org/2001/04/xmlenc#" Type="http://www.w3.org/2001/04/xmlenc#Element"><xenc:EncryptionMethod Algorithm="http://www.w3.org/2001/04/xmlenc#aes256-cbc"/><KeyInfo xmlns="http://www.w3.org/2000/09/xmldsig#"><e:EncryptedKey xmlns:e="http://www.w3.org/2001/04/xmlenc#"><e:EncryptionMethod Algorithm="http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p"><DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/></e:EncryptionMethod><KeyInfo><ds:X509Data xmlns:ds="http://www.w3.org/2000/09/xmldsig#"><ds:X509IssuerSerial><ds:X509IssuerName>CN=CERN Grid Certification Authority, DC=cern, DC=ch</ds:X509IssuerName><ds:X509SerialNumber>113927968756732597970763</ds:X509SerialNumber></ds:X509IssuerSerial></ds:X509Data></KeyInfo><e:CipherData><e:CipherValue>OjGtcs9eqK3ZyipR981nYl88GO+sCQLXbxWTJQx4BqJwrFqWGPDsHhNKwzjRMC6P6zbphaWnf9ANE+xxmG1dleI8OsIEWsIFUV3+XGyjvTX9oeSWKyjcNDV3DDFUymO/b/rNBP1fL6hCz6n2gIt7hTxD4UR/9lR4HcJNM475JOqbCwjU4DEaH4kr9hqX9ASCN97GLQ9fDnARHE3nCxaO/KMp7Wi+UD24Az35VrSIIx20CvODBbIcVU3VTnTncyUn/Otp7x2h3fVhf3vOcR7F1RmwunwST9Bd0CGrq0gRYZP1KlO2MUjN2UqqRtjDpoBCQCfyLmHiiRQKwNGU+JMyHw==</e:CipherValue></e:CipherData></e:EncryptedKey></KeyInfo><xenc:CipherData><xenc:CipherValue>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</xenc:CipherValue></xenc:CipherData></xenc:EncryptedData></EncryptedAssertion></samlp:Response>;org.opensaml.common.SAMLException: Response doesn't have any valid assertion which would pass subject validation
