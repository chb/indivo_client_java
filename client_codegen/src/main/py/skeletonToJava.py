import api_skeleton
import use_ast

"""
Each implementation of the query-api passes in a list of valid fields. See
(for example) the LAB_FILTERS variable in
indivo_server/indivo/views/reports/lab.py. Definitions take the form of:

'public-facing-name': ('internal-name', datatype)

So that a user of a client should be able to call (i.e.):

get_labs(record_id, querystring='?public-facing-name=VALUE'),
Where VALUE is of type 'datatype'

-Dan


On 12/23/11 4:38 PM, "Finstein, Nathan"
<Nathan.Finstein@childrens.harvard.edu> wrote:

> Dan,
> 
>   Is there a machine readable source for the lists of "Valid Query Fields" in
> query-api.html ?
> 
>            Nate
> 
> 
"""


"""
reports/minimal/allergies/
reports/minimal/measurements/{LAB_CODE}/
reports/minimal/vitals/{CATEGORY}"
audits/query/


"path":"/carenets/{CARENET_ID}/reports/minimal/vitals/{CATEGORY}"
"path":"/records/{RECORD_ID}/reports/minimal/vitals/{CATEGORY}/"

String: a string of text. All comparisons, groups, and filters against this field will be conducted using string operations.
Date: an iso8601 UTC formatted datetime. All comparisons, groups, and filters against this field will be conducted using date operations.
Number: an integer or floating point field. All comparisons, groups, and filters against this field will be conducted using numerical operations.


"""

slst_soaddglo = ['status', 'order_by', 'aggregate_by',
        'date_range', 'date_group', 'group_by', 'limit', 'offset']
slst_soaddglo.sort()
slst_stolo = ['status', 'type', 'order_by', 'limit', 'offset']
slst_stolo.sort()
slst_solo = ['status', 'order_by', 'limit', 'offset']
slst_solo.sort()
slst_olo = ['order_by', 'limit', 'offset']
slst_olo.sort()

common_qo_map = {
        tuple(slst_soaddglo):"soaddglo",
        tuple(slst_stolo):"stolo",
        tuple(slst_solo):"solo",
        tuple(slst_olo):"olo"
}
common_qo_keys_l = common_qo_map.keys()
common_qo = []
for cqk in common_qo_keys_l:
    common_qo.append(set(cqk))
qo_counts = [0,0,0,0]


def write_common_opts(rest0, report_query_fields):
    rest0.write("private Map<String, List<String>> commonOptionsMap")
    rest0.write(" = new HashMap<String,List<String>>();\n{\n")
    for cov, cok in common_qo_map.items():
        rest0.write("    commonOptionsMap.put(\"" + cok + "\", Arrays.asList(")
        nfdelim = None
        for acov in cov:
            if nfdelim:
                rest0.write(nfdelim)
            else:
                nfdelim = ", "
            rest0.write("\"" + acov + "\"")
        rest0.write("));\n")
    rest0.write("    }\n\n")
    
    rest0.write("private Map<String, List<String>> validQueryFields")
    rest0.write(" = new HashMap<String,List<String>>();\n")
    rest0.write("private Map<String, Class> queryFieldType")
    rest0.write(" = new HashMap<String, Class>();\n{\n")
    rest0.write("    List<String> vqfs = null;\n")
    queryFieldTypes = {}
    for qfk, qfv in report_query_fields.items():
        rest0.write("    vqfs = Arrays.asList(")
        for aqfi, aqfv in enumerate(qfv):
            if aqfi:
                rest0.write(", ")
            rest0.write("\"" + aqfv[0] + "\"")
             
            qfvt = None;
            if aqfv[1] == "STRING":
                qfvt = "String.class"
            elif aqfv[1] == "DATE":
                qfvt = "Date.class"
            elif aqfv[1] == "NUMBER":
                qfvt = "Number.class"
            else:
                print("unexpected query field type: " + aqfv[0] + ": " + aqfv[1])
                raise Exception
            if queryFieldTypes.has_key(aqfv[0]):
                if queryFieldTypes[aqfv[0]] != qfvt:
                    print("inconsistent type for query field " + aqfv[0] + ": " + queryFieldTypes[aqfv[0]] + " and " + qfvt)
                    raise Exception
            else:
                queryFieldTypes[aqfv[0]] = qfvt
                
        rest0.write(");\n")
        rest0.write("    validQueryFields.put(\"" + qfk + "\", vqfs);\n")
    rest0.write("\n")
    for aqfvk in queryFieldTypes.keys():
        rest0.write("    queryFieldType.put(\"" + aqfvk + "\", \"" + queryFieldTypes[aqfvk] + "\");\n")
    rest0.write("}\n\n")


def get_report_flavors():
    report_flavors = []        
    for acall in api_skeleton.CALLS:
        path = str(acall["path"])
        assert path.startswith("/")
        path = path[1:]
        pathparts = path.split('/')
        pplen = len(pathparts)
        if (pplen == 6 and pathparts[pplen -1] == '' and pathparts[pplen -4] == "reports"
             and pathparts[pplen -3] == "minimal" and pathparts[pplen -2][0] != '{'):
            try:
                report_flavors.index(pathparts[pplen -2])
            except ValueError:
                report_flavors.append(pathparts[pplen -2])
    report_fields = use_ast.all_reports("/home/nate/indivo/github")
    report_fields_sn = {}
    for rfkey in report_fields.keys():
        usix = rfkey.find('_')
        rfkeyt = rfkey[0:usix] # drop "_FILTERS"
        if rfkeyt[len(rfkeyt) -1] == 'S':
            rfkeyt = rfkeyt[0:len(rfkeyt) -1]
        rfkeyt = rfkeyt.lower()
        report_fields_sn[rfkeyt] = report_fields[rfkey]
    report_flavors_sn = []
    for rfws in report_flavors:
        print "rfws: " + rfws
#rfws: allergies
#rfws: simple-clinical-notes
        if rfws == "allergies":
            report_flavors_sn.append("allergy")
        elif rfws == "simple-clinical-notes":
            report_flavors_sn.append("simple")
        elif rfws[len(rfws) -1] == 's':
            report_flavors_sn.append(rfws[0:len(rfws) -1])
        else:
            report_flavors_sn.append(rfws)
    print "missing from skeleton: " + str((set(report_fields_sn.keys())) - set(report_flavors_sn))
    print "missing from reports/*.py: " + str((set(report_flavors_sn) - set(report_fields_sn.keys())))
    return report_flavors_sn, report_fields_sn


def combine_reports_minimal(reports_minimal_records, reports_minimal_carenet, pathparts):
    pplen = len(pathparts)
    redundant_report_minimal = False
    first_report_minimal = False
    audit_query = False
    if (((pathparts[0] == "records" and reports_minimal_records) or (pathparts[0] == "carenets" and reports_minimal_carenet))
         and pplen == 6 and pathparts[pplen -1] == '' and pathparts[pplen -4] == "reports"
         and pathparts[pplen -3] == "minimal" and pathparts[pplen -2][0] != '{'):
        redundant_report_minimal = True
    elif (pplen == 6 and pathparts[pplen -1] == '' and pathparts[pplen -4] == "reports"
         and pathparts[pplen -3] == "minimal" and pathparts[pplen -2][0] != '{'):
        first_report_minimal = True
        if pathparts[0] == "records":
            reports_minimal_records = True  # only one of these needed, do no others
        elif pathparts[0] == "carenets":
            reports_minimal_carenet = True  # only one of these needed, do no others
        
    elif (pplen == 5) and pathparts[pplen -2] == "query" and pathparts[pplen -3] == "audits":
        audit_query = True
    
    return reports_minimal_records, reports_minimal_carenet, redundant_report_minimal, first_report_minimal, audit_query

def make_method_name(pathparts, httpmeth):
    method_name = ''
    params = []
        
    for ppart in pathparts:
        if ppart.startswith('{'):
            assert ppart.endswith('}')
            ppart0 = 'X'
            params.append(ppart[1:len(ppart) -1])
        else:
            ppart0 = dashToCamel(ppart)
            
        if len(method_name):
            method_name += '_'
        method_name += ppart0

    method_name += httpmeth
    return method_name, params

def javadoc(params, url_params, report_flavors, legged, put_post_data, put_post_data_form, response_form):        
    retVal = ''
    retVal += "    /**\n"
    for aparm in params:
        retVal += ("     * @param " + var_java_style(aparm) + " ")
        if aparm == "REPORT_FLAVOR":
            retVal += ("one of: " + ", ".join(report_flavors))
        else:
            retVal += url_params[aparm]
        retVal += '\n'
    if legged == 3:
        retVal += "     * @param accessToken OAuth token.\n     * @param accessTokenSecret OAuth secret.\n"
    elif legged > 2:
        retVal += """     * @param accessToken OAuth token. null if from admin app.
     * @param accessTokenSecret OAuth secret. null if from admin app.\n"""
    
    if put_post_data:
        retVal += "     * @param body data to send"
        if put_post_data_form:
            retVal += (", must be in " + put_post_data_form + " form.\n")
        else:
            retVal += "\n     * @param requestContentType mime type of body.\n"
        
    if response_form == "unknown":
        retVal += """     * @param responseContentType expected mime type, for example "application/xml" or "text/plain"
                   will cause exception to be thrown if expectation does not match server response
                   may be null to allow any type"""

    retVal += """\
     * @param options Map<String,Object> or null.
     *       allowed key-value pairs are:
     *            "responseTypeConversion": object that implements ResponseTypeConversion,
     *            "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret
     *                                  Use this to access an indivo installation other then one provided by new Rest(...)
     *            "connectionTimeout": integer,
     *            "socketTimeout": integer
"""
                   
    return retVal + '    */\n'

def categorize_query_options(query_opts, httpmeth):
    qopts_r = []
    qopts_o = []
    qopts_field = False
    
    if query_opts and httpmeth != "GET":
        print("query_opts on non GET:" + str(query_opts))
    elif query_opts:
        for qok, qov in query_opts.items():
            if qok == """{FIELD}""":
                #document_id: The document modified by the request. String
                #external_id: The external id used to reference a resource in the request. String
                #request_date: The date on which the request was made. Date
                #function_name: The internal Indivo X view function called by the request. String
                #principal_email: The email of the principal making the request. String
                #proxied_by_email: The email of the principal proxied by the principal making the request (i.e., the email of the 
                qopts_field = True
            elif qov.find("*REQUIRED*") == -1:
                qopts_o.append(qok)
            else:
                qopts_r.append(qok)
    return qopts_r, qopts_o, qopts_field


def method_params(qopts_r, qopts_o, params, url_params, qopts_field, legged, first_report_minimal, audit_query):
    qoptparams = ''
    for qoptr in qopts_r:
        if qoptparams:
            qoptparams += ", "
        qoptparams += ("String " + qoptr)
    
    has_queryoptions = False
    if qopts_o:
        if qoptparams:
            qoptparams += ", "
        qoptparams += ("String queryOptions")
        has_queryoptions = True
        
    #has_report_flavor = False
    for aparam in params:
        assert aparam in url_params or aparam == "REPORT_FLAVOR"
        if aparam == "REPORT_FLAVOR":
            assert qopts_field
        else:
            if qoptparams:
                qoptparams += ", "
            qoptparams += ("String " + var_java_style(aparam))
    
    if first_report_minimal:
        if qoptparams:
            qoptparams += ", "
        # records: measurements, vitals
        # carenet: "", measurements
        # /carenets/{CARENET_ID}/reports/minimal/vitals/
        # /carenets/{CARENET_ID}/reports/minimal/vitals/{CATEGORY}
        # /records/{RECORD_ID}/reports/minimal/vitals/
        # /records/{RECORD_ID}/reports/minimal/vitals/{CATEGORY}/
        #  /records/{RECORD_ID}/reports/minimal/measurements/{LAB_CODE}/
        #/carenets/{CARENET_ID}/reports/minimal/measurements/{LAB_CODE}/
        qoptparams += "String reportFlavor"

    if audit_query:
        if qoptparams:
            qoptparams += ", "
        qoptparams += "String auditQueryField"
            
    #(
    #rest.write(qoptparams)
    if legged > 2:
        if qoptparams:
            qoptparams += ", "
        qoptparams += "String accessToken, String accessTokenSecret"
        
    return qoptparams


def make_url(pathparts):
    retVal = ''

    # assuming here never starts with a variable
    assert pathparts[0][0] != '{'
    last_token_type = None
    for ppart in pathparts:
        if last_token_type == "trailSlash":
            raise Exception
        if ppart:
            if ppart[0] == '{':
                if last_token_type == None:
                    raise Exception # not necessarily wrong, but leading variable not anticipated
                #rest.write(" + '/' + " + var_java_style(ppart[1:len(ppart) -1]))
                if last_token_type == "const":
                    retVal += "/\""
                elif last_token_type == "var":
                    retVal += " + '/'"
                retVal += (" + " + var_java_style(ppart[1:len(ppart) -1]))
                last_token_type = "var"
            else:
                if last_token_type == "const":
                    retVal += '/'
                elif last_token_type == "var":
                    retVal += " + \"/"
                elif last_token_type == None:
                    retVal += "\""
                retVal += ppart
                last_token_type = "const"
                
        else:
            if last_token_type == "const":
                pass
            elif last_token_type == "var":
                retVal += " + \""
            retVal += "/\""
            last_token_type = "trailSlash"
    if last_token_type == "const":
        retVal += '"'
            
    return retVal
#               "POST", "records/" + recordId + "/documents/" + documentId + "/set-status",
#               queryOut, accessToken, accessTokenSecret, buildFormURLEnc(new String[][] {{ "reason", reason }, { "status", status } }), "application/x-www-form-urlencoded", options);
#        return fromRequest;


def request_query(qopts_r, qopts_o):
    """
    /** convenience method for where no request body is sent
     * and the return type is "application/xml"
    */
      public Object clientRequest(
            String reqMeth,
            String reletivePath,
            Object queryString,
            String phaToken,
            String phaTokenSecret,
            Map<String,Object> options)
            
    /** conveninece method for where no request body is sent */
    public Object clientRequest(
            String reqMeth,
            String reletivePath,
            Object queryString,
            String phaToken,
            String phaTokenSecret,
            Object responseContentType,
            Map<String,Object> options)
            
    /** conveninece method for where return type is "application/xml" */
    public Object clientRequest(
            String reqMeth,
            String reletivePath,
            Object queryString,
            String phaToken,
            String phaTokenSecret,
            Object requestBody,   // String or byte[]
            String requestContentType,
            Map<String,Object> options)
        
        ---------------------------------------
          public Object clientRequest(
            String reqMeth,
            String reletivePath,
            Object queryString,
            String phaToken,
            String phaTokenSecret,
            Object requestBody,   // String or byte[]
            String requestContentType,
            Object responseContentType,
            Map<String,Object> options)
        """
    querystring = ''
    for qoptr in qopts_r:
        if querystring:
            querystring += " + '&' + "
        querystring += qoptr
    if qopts_o:
        if querystring:
            querystring += " + '&' + "
        querystring += "queryOptions"
    if not querystring:
        querystring = "\"\""
        
        
        
    return querystring

def decide_data_form(datafields, dfekvm, httpmeth, path):
#datafields'': The raw XML attachment data.: 1
#datafields'': A valid Indivo Contact Document (see :doc:`/schemas/contact-schema`).: 2
#datafields'': The raw content of the document to create/update.: 2
#datafields'': The raw content of the document to create.: 13
#datafields'': Raw content that will be used as a setup document for the record. **OPTIONAL**.: 1
#datafields'': The email address of the new account owner.: 2
#datafields'': The new label for the document: 4
    dfkeys = datafields.keys()
    
    if httpmeth in ["GET", "DELETE"] and datafields:
        print("GET/DELETE with data_fields: " + httpmeth + " " + path + " -- " + datafields)
    elif httpmeth in ["PUT", "POST"] and not datafields:
        print("PUT/POST without data_fields: " + httpmeth + " " + path)
    elif datafields:
        if len(dfkeys) == 1 and dfkeys[0] == "":
            dfekv = datafields[""]
            if dfekvm.has_key(dfekv):
                dfekvm[dfekv] = dfekvm[dfekv] +1
            else:
                dfekvm[dfekv] = 1
                
    put_post_data = False
    put_post_data_form = None
    if dfkeys:
        put_post_data = True
        if dfkeys[0]:
            put_post_data_form = "url_encoded" # actual key/value pair expected
        else:
            assert len(dfkeys) == 1
            dfval = datafields[""].lower()
            if dfval.find("raw content") != -1:
                pass #put_post_data_form = None
            elif dfval.find("XML") != -1 or dfval.find("schema") != -1:
                put_post_data_form = "application/xml"
            else:
                put_post_data_form = "text/plain"
                    
    return put_post_data, put_post_data_form 


def decide_response_form(retex, retdsc, httpmeth, path):
    """ returns "XML", "JSON", "URL_ENCODED", "PLAIN_TEXT" or "unknown" """
    retVal = "unknown"
    
    #</ok> (sic)
    if retex == "<ok/>" or retex == "</ok>" or (retex[0] == '<' and
         (retdsc == ":http:statuscode:`200` with a list of "
         or retdsc.startswith(":http:statuscode:`200` with A list of ")
         or retdsc.startswith(":http:statuscode:`200` with a document list") )
         ):
        retVal = "XML"
    elif retdsc.find("`200`, with XML describing") != -1 or retdsc.find("`200` with XML describing") != -1:
        retVal = "XML"
    elif retex[0] == '<' and (retdsc.find("`200`, with information about") != -1
                                or retdsc.find("`200` with information about") != -1
                                or retdsc.find("`200` with the special document's raw content") != -1):
        retVal = "XML"
    elif retex.startswith("<secret>"):
        retVal = "XML"
    elif retex.startswith("<Document id") and (
            retdsc.find("`200` with the metadata of the") != -1
            or retdsc.find("`200` with metadata describing the") != -1
            or retdsc.find("`200` with the document metadata")
            or retdsc.find("`200` with the document's metadata")
            or retdsc.find("`200` with the metadata on the new")
            or retdsc.find("`200` with the metadata on the updated")
            ):
        retVal = "XML"
    elif retex.startswith("<Carenets ") and retdsc.find("`200` with a description of the new carenet") != -1:
        retVal = "XML"
    elif retex.startswith("<DocumentStatusHistory ") and retdsc.find("`200` with a the document's status history") != -1:
        retVal = "XML"
    elif retex.startswith("<Account id="):
        retVal = "XML"
    elif retex.startswith("<ContinuityOfCareRecord "):
        retVal = "XML"
    elif retdsc.find("`200` with the current version of Indivo.") != -1:
        retVal = "XML"
#    "return_ex":'''
#1.0.0.0

        retVal = "XML"
    elif retex[0] == '[' and retdsc.find("`200` with JSON") != -1:
        retVal = "JSON"
    elif retex.startswith("oauth_token=") and (
            retdsc.find("`200` with a valid session token") != -1
            or retdsc.find("`200` with the request token") != -1
            or retdsc.find("`200` with a valid access token") != -1
            or retdsc.find("`200` with an access token") != -1
            ):
        retVal = "URL_ENCODED"
    elif retex.startswith("location=") and retdsc.find("`200` with a redirect url") != -1:
        retVal = "URL_ENCODED"
    elif retex.find('@') != -1 and retdsc.find("`200` with the email") != -1:
        retVal = "PLAIN_TEXT"
    else:
        print("undecided response format: " + httpmeth + " " + path + ": " + retex + "    --    " + retdsc)
        
    return retVal

def decide_oauth_legged(access_doc):
    legged = None
    if access_doc in [
        "A user app with an id matching the app email in the URL.",
        "Any admin app.",
        "An admin app with an id matching the principal_email in the URL.",
        "A request signed by a RequestToken.",
        "Any principal in Indivo.",
        "The user app itself."]:
        legged=2.0
    elif access_doc in [
"A principal in full control of the record, or any admin app.",
"Any principal in Indivo.",
"Any admin app, or a user app with access to the record.",
"Any admin app, or the Account owner.",
"A principal in full control of the record, the admin app that created the record, or a user app with access to the record.",
"Any admin app, or a principal in full control of the record.",
"A user app with access to the carenet or the entire carenet's record, an account in the carenet or in control of the record, or the admin app that created the carenet's record.",
"A principal in full control of the carenet's record.",
"A principal in the carenet, in full control of the carenet's record, or any admin app.",
"A user app with access to the carenet and proxying the account, a principal in full control of the carenet's record, or any admin app.",
"A user app with access to the record, a principal in full control of the record, or the admin app that created the record.",
"Anybody",
"The owner of the record, or any admin app."
       ]:
        legged = 2.5   # could be two or three legged
    else:
        legged = 3.0
    
    return legged


#options = {
#        "responseTypeConversion": "object that implements ResponseTypeConversion",
#        "indivoInstallation":
#           "foreignURL = indivoInstallation0[0]; consumerToken = indivoInstallation0[1]; consumerSecret = indivoInstallation0[2];",
#        "connectionTimeout": "integer",
#        "socketTimeout": "integer"
#        }

def process_calls(rest, report_flavors):
    reports_minimal_records = False
    reports_minimal_carenet = False
    access_doc_map = {}
    call_count = 0
    dfekvm = {}
    retexs = {}
    retdscs = {}
    okcount = 0
    
    for acall in api_skeleton.CALLS:
        call_count += 1
        httpmeth = acall["method"]
        assert httpmeth in ["GET", "PUT", "POST", "DELETE"]
        
        retex = acall["return_ex"]
        assert isinstance(retex, str)
        retex = retex.strip()[:50]
        if retexs.has_key(retex):
            retexs[retex] = retexs[retex] +1
        else:
            retexs[retex] = 1

        retdsc = acall["return_desc"]
        assert isinstance(retdsc, str)
        
#        if retdsc.find("http:statuscode:`200` with a list of ") != -1:
#            print("retdsc found: " + retdsc)
        
        if retex == "<ok/>":
            okcount += 1
        else:
            if retdsc.startswith(":http:statuscode:`200` with a list of ") or retdsc.startswith(":http:statuscode:`200`, with a list of "):
                retdsc = ":http:statuscode:`200` with a list of "
            if retdscs.has_key(retdsc):
                retdscs[retdsc] = retdscs[retdsc] +1
            else:
                retdscs[retdsc] = 1
                        
        deprecated = acall["deprecated"]
        access_doc = acall["access_doc"]
        legged = decide_oauth_legged(access_doc)
            
        # not used, just count occurrences in case the statistic is ever needed
        adcount = access_doc_map.get(access_doc)
        if adcount:
            access_doc_map[access_doc] = adcount +1
        else:
            access_doc_map[access_doc] = 1

        path = acall["path"]  #str(acall["path"])
        
        response_form = decide_response_form(retex, retdsc, httpmeth, path)

        
        assert path.startswith("/")
        path = path[1:]
        pathparts = path.split('/')

        
#Unfortunately, this is actually the way the call is written into Indivo
#Server. In future releases, we'll deprecate the call and replace it with the
#correct one, but for now the documentation is actually accurate.  -Dan
#> "path":"/carenets/{CARENET_ID}/reports/minimal/vitals/{CATEGORY}"
#> "path":  "/records/{RECORD_ID}/reports/minimal/vitals/{CATEGORY}/"
        
        (reports_minimal_records,
         reports_minimal_carenet,
         redundant_report_minimal,
         first_report_minimal,
         audit_query) = combine_reports_minimal(
                            reports_minimal_records, reports_minimal_carenet, pathparts)
        
        # if redundant_report_minimal and not first of its kind
        if redundant_report_minimal:
            continue
        # first of its kind, process it but not next time
        elif first_report_minimal:
            print("first_report_minimal: " + repr(pathparts))
            pathparts[len(pathparts) -2] = "{REPORT_FLAVOR}"   ############
            print("first_report_minimal: " + repr(pathparts))
#        print "continuing " + ' '.join(pathparts)

        
        method_name, params = make_method_name(pathparts, httpmeth)
            
        url_params = acall["url_params"]
        
        datafields = acall["data_fields"]
        assert isinstance(datafields, dict)
        put_post_data, put_post_data_form = decide_data_form(datafields, dfekvm, httpmeth, path)        

        rest.write(javadoc(params, url_params, report_flavors, legged, put_post_data, put_post_data_form, response_form))
        
        query_opts = acall["query_opts"]
        qopts_r, qopts_o, qopts_field = categorize_query_options(query_opts, httpmeth)
            
        rest.write("    public Object " + method_name + "(\n            ") #)
        
        mthprms = method_params(qopts_r, qopts_o, params, url_params, qopts_field, legged, first_report_minimal, audit_query)
        #rest.write(mthprms)
        
        if put_post_data:
            if mthprms:
                mthprms += ", "
            mthprms += "Object body"
            if not put_post_data_form:  # not known, caller must specify
                mthprms += ", String requestContentType"
        
        if response_form == "unknown":
            if mthprms:
                mthprms += ", "
            mthprms += "Object responseContentType"
            
        if mthprms:
            mthprms += ", "
        mthprms += "Map<String, Object> options"
        rest.write(mthprms)
                
        rest.write(') {\n')
        
        if qopts_o:
            options_str1, options_str2 = process_query_opts(qopts_o, qopts_field, first_report_minimal, audit_query)
            rest.write(options_str1)
            rest.write(options_str2)
                
        rest.write("        Object fromRequest = clientRequest(\n                ")
        rest.write("\"" + httpmeth + "\", ")

        madeurl = make_url(pathparts)
        rest.write(madeurl)
        
        reqquer = request_query(qopts_r, qopts_o)
        rest.write(", " + reqquer)
        
        if legged > 2:
            rest.write(", accessToken, accessTokenSecret")
        else:
            rest.write(", null, null")

        if put_post_data:
            rest.write(", body")
            if put_post_data_form:
                if put_post_data_form == "url_encoded":
                    rest.write(", \"application/x-www-form-urlencoded\"")
                elif put_post_data_form == "application/xml":
                    rest.write(", \"application/xml\"")
                elif put_post_data_form == "text/plain":
                    rest.write(", \"text/plain\"")
                else:
                    raise Exception
            else:
                rest.write(", requestContentType")
                
        # "URL_ENCODED", "PLAIN_TEXT"
        if response_form == "unknown":
            rest.write(", reponseContentType")
        elif response_form == "XML":
            rest.write(", \"application/xml\"")
        elif response_form == "JSON":
            rest.write(", \"application/json\"")
        elif response_form == "URL_ENCODED":
            rest.write(", \"application/x-www-form-urlencoded\"")
        elif response_form == "PLAIN_TEXT":
            rest.write(", \"text/plain\"")
        else:
            raise Exception
        
        rest.write(");\n        return fromRequest;\n")            
        rest.write("    }\n\n")
    print(qo_counts[0], common_qo[0])
    print(qo_counts[1], common_qo[1])
    print(qo_counts[2], common_qo[2])
    print(qo_counts[3], common_qo[3])

    print("report flavors: " + str(report_flavors))
    for adv in access_doc_map.keys():
        print adv + ": " + str(access_doc_map[adv])
    print("call count: " + str(call_count))
    
    dfekvmK = dfekvm.keys()
    for mK in dfekvmK:
        print("datafields'': " + mK + ": " + str(dfekvm[mK]))

    retexK = retexs.keys()
    for rK in retexK:
        print("response example: " + rK + ": " + str(retexs[rK]))

    print("OK count: " + str(okcount))
    retdscK = retdscs.keys()
    for reK in retdscK:
        print(str(retdscs[reK]) + ": response description: " + reK)


def writeprefix(prefixLines, rest):
    for pline in prefixLines:
        plinestr = str(pline)
        if plinestr.strip().startswith("/***START AUTO GENERATED FROM WIKI*/"):
            break
        shix = plinestr.find("_SHELL")
        if shix != -1:
            plinestr = plinestr[:shix] + plinestr[shix +6:]
        drix = plinestr.find("/*_DROP*/")
        if drix == -1:
            rest.write(plinestr)

    
def writesuffix(prefixLines, rest):
    dowrite = False
    for pline in prefixLines:
        plinestr = str(pline)
        if plinestr.strip().startswith("/***END AUTO GENERATED FROM WIKI*/"):
            dowrite = True        
        elif dowrite:
            shix = plinestr.find("_SHELL")
            if shix != -1:
                plinestr = plinestr[:shix] + plinestr[shix +6:]
            drix = plinestr.find("/*_DROP*/")
            if drix == -1:
                rest.write(plinestr)

def process_query_opts(query_opts0, qopts_field, report_minimal, audit_query):
    query_opts = list(query_opts0)
    query_opts.sort()
    qo_set = set(query_opts)
    if qo_set:
        if qo_set in common_qo:
            qoix = common_qo.index(qo_set)
            qo_counts[qoix] += 1
            qo_set_name = common_qo_map.get(tuple(query_opts))
            optional_str_1 = ""
            optional_str_2a = "commonOptionsMap.get(\"" + qo_set_name + "\")"
        else:
            print('unexpected query_opts0 set: ' + str(qo_set))
            optional_str_1 = "        List<String> optional = Arrays.asList("
            for qoki, qok in enumerate(query_opts):
                if qoki:
                    optional_str_1 += ", "
                optional_str_1 += ('"' + qok + '"')
            optional_str_1 += ");\n"
            optional_str_2a = "optional"
            #validQueryFields.get(reportFlavor)
        if report_minimal:
            optional_str_2b = "validQueryFields.get(reportFlavor)"
        elif audit_query:
            optional_str_2b = "auditQueryFields"
        else:
            optional_str_2b = "null"
            
        optional_str_2 = "        checkQueryOptions(queryOptions, " + optional_str_2a + ", " + optional_str_2b + ");\n"
    else:
        optional_str_1 = ""
        optional_str_2 = "null"
    
    return optional_str_1, optional_str_2

def var_java_style(origvar):
    var0 = origvar.lower()
    retVal = ""
    usix = var0.find('_')
    while usix != -1:
        retVal += var0[:usix]
        var0 = var0[usix +1:]
        if var0:
            retVal += var0[0].upper()
            var0 = var0[1:]
        usix = var0.find('_')
    retVal += var0
    return retVal

def dashToCamel(frag):
    """Java does not allow '-' a part of a method name """
    retVal = frag
    dix = retVal.find("-")
    while dix != -1:
    #System.err.println("dash to camel: " + retVal.toString());
    # will fail if trailing dash
        uc = retVal[dix +1].upper()
        retVal = retVal[0:dix] + uc + retVal[dix +2:] # replace "-x" with "X"
        dix = retVal.find("-")
    return retVal



rest0 =   open("/home/nate/java/eclipse/workspace/JavaClientForIndivo_and_test/src/org/indivo/client/REST.java", "w")
prefix = open("/home/nate/java/eclipse/workspace/JavaClientForIndivo_and_test/src/org/indivo/client/Rest_SHELL.java","r")
prefixSuffix = prefix.readlines()
writeprefix(prefixSuffix, rest0)
repflv, repqflds = get_report_flavors()
write_common_opts(rest0, repqflds)
print("report flavors len: " + str(len(repflv)))
process_calls(rest0, repflv)
writesuffix(prefixSuffix, rest0)
print "done all"
