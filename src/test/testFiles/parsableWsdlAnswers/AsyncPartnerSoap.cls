//Generated by wsdl2apex

public class AsyncPartnerSoap {
    public class describeSObjectResponse_elementFuture extends System.WebServiceCalloutFuture {
        public partnerSoap.DescribeSObjectResult getValue() {
            partnerSoap.describeSObjectResponse_element response = (partnerSoap.describeSObjectResponse_element)System.WebServiceCallout.endInvoke(this);
            return response.result;
        }
    }
    public class AsyncSoap {
        public String endpoint_x = 'https://login.salesforce.com/services/Soap/u/10.0';
        public Map<String,String> inputHttpHeaders_x;
        public String clientCertName_x;
        public Integer timeout_x;
        private String[] ns_map_type_info = new String[]{'urn:sobject.partner.soap.sforce.com', 'sParnterSoap', 'urn:partner.soap.sforce.com', 'partnerSoap'};
        public AsyncPartnerSoap.describeSObjectResponse_elementFuture beginDescribeSObject(System.Continuation continuation,String sObjectType) {
            partnerSoap.describeSObject_element request_x = new partnerSoap.describeSObject_element();
            request_x.sObjectType = sObjectType;
            return (AsyncPartnerSoap.describeSObjectResponse_elementFuture) System.WebServiceCallout.beginInvoke(
              this,
              request_x,
              AsyncPartnerSoap.describeSObjectResponse_elementFuture.class,
              continuation,
              new String[]{endpoint_x,
              '',
              'urn:partner.soap.sforce.com',
              'describeSObject',
              'urn:partner.soap.sforce.com',
              'describeSObjectResponse',
              'partnerSoap.describeSObjectResponse_element'}
            );
        }
    }
}