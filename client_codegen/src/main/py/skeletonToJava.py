import api_skeleton
import api_skel_auxiliary
import use_ast
import sys
import process_api_xml
import xml.dom.minidom

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
#qo_counts = [0,0,0,0]


def write_common_opts(rest0, pynames, report_query_fields):
    write_both(rest0, pynames, "private Map<String, List<String>> commonOptionsMap")
    write_both(rest0, pynames, " = new HashMap<String,List<String>>();\n{\n")
    for cov, cok in common_qo_map.items():
        write_both(rest0, pynames, "    commonOptionsMap.put(\"" + cok + "\", Arrays.asList(")
        nfdelim = None
        for acov in cov:
            if nfdelim:
                write_both(rest0, pynames, nfdelim)
            else:
                nfdelim = ", "
            write_both(rest0, pynames, "\"" + acov + "\"")
        write_both(rest0, pynames, "));\n")
    write_both(rest0, pynames, "    }\n\n")
    
    write_both(rest0, pynames, "private Map<String, List<String>> validQueryFields")
    write_both(rest0, pynames, " = new HashMap<String,List<String>>();\n")
    write_both(rest0, pynames, "private Map<String, Class> queryFieldType")
    write_both(rest0, pynames, " = new HashMap<String, Class>();\n{\n")
    write_both(rest0, pynames, "    List<String> vqfs = null;\n")
    queryFieldTypes = {}
    for qfk, qfv in report_query_fields.items():
        write_both(rest0, pynames, "    vqfs = Arrays.asList(")
        for aqfi, aqfv in enumerate(qfv):
            if aqfi:
                write_both(rest0, pynames, ", ")
            write_both(rest0, pynames, "\"" + aqfv[0] + "\"")
             
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
                
        write_both(rest0, pynames, ");\n")
        write_both(rest0, pynames, "    validQueryFields.put(\"" + qfk + "\", vqfs);\n")
    write_both(rest0, pynames, "\n")
    for aqfvk in queryFieldTypes.keys():
        write_both(rest0, pynames, "    queryFieldType.put(\"" + aqfvk + "\", " + queryFieldTypes[aqfvk] + ");\n")
    write_both(rest0, pynames, "}\n\n")


def get_report_flavors(indivo_server_path):
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
    report_fields = use_ast.all_reports(indivo_server_path) #"/home/nate/indivo/github"
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
        #print "rfws: " + rfws
        if rfws == "allergies":
            report_flavors_sn.append("allergy")
        elif rfws == "simple-clinical-notes":
            report_flavors_sn.append("simple")
        elif rfws[len(rfws) -1] == 's':
            report_flavors_sn.append(rfws[0:len(rfws) -1])
        else:
            report_flavors_sn.append(rfws)
    #print "missing from skeleton: " + str((set(report_fields_sn.keys())) - set(report_flavors_sn))
    #print "missing from reports/*.py: " + str((set(report_flavors_sn) - set(report_fields_sn.keys())))
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

def javadoc(params, report_flavors, legged, put_post_data, put_post_data_form, response_form, dividN, acall):
    url_params = acall.get("url_params") #dict
    divid = "o" + str(dividN)
    retVal = ''
    retVal += "    /**\n"
    description = acall.get("description").strip()
    if description[len(description) -1:] != '.':
        description += "."
    retVal += "     * " + acall.get("description") + "<br/>\n"
    retVal += """<a href="" onclick="document.getElementById('{0}').style.display='inline'; return false;"
>details</a>
<div id="{0}" style="display:none">\n""".format(divid)
    retVal += "     * <code>" + acall.get("method") + " " + acall.get("path") + "<\code><br/>\n" 
    retVal += "     *  accessibility: " + acall.get("access_doc") + "<br/>\n"
    for aparm in params:
        retVal += "     * @param " + var_java_style(aparm) + " "
        if aparm == "REPORT_FLAVOR":
            retVal += "one of: " + ", ".join(report_flavors)
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
            retVal += ", must be in " + put_post_data_form + " form.\n"
        data_fields = acall.get("data_fields")
        if data_fields:
            for dfk in data_fields.keys():
                retVal += "     * "
                if dfk:
                    retVal += dfk + ": "
                retVal += data_fields.get(dfk) + "<br/>\n"
                
        if not put_post_data_form:
            retVal += "\n     * @param requestContentType mime type of body.\n"
        
    if response_form == "unknown":
        retVal += """     * @param responseContentType expected mime type, for example "application/xml" or "text/plain"
                   will cause exception to be thrown if expectation does not match server response
                   may be null to allow any type"""

    retVal += """\
     * @param options Map<String,Object> or null.
allowed key-value pairs are<br/>:
    "responseTypeConversion": object that implements ResponseTypeConversion,
    "indivoInstallation": String array of length 3: foreignURL, consumerToken, consumerSecret.
                           Use this to access an indivo installation other then one provided by new Rest(...)
    "connectionTimeout": integer,
    "socketTimeout": integer

"""
    return_desc = acall.get("return_desc")
    return_ex = acall.get("return_ex")
    if return_desc or return_ex:
        retVal += "     * @return " + return_desc.replace("*/*", "* / *")
        if return_ex:
            retVal += "<br/>\n<pre>" + return_ex + "</pre>\n"
            
    deprecated, added, changed = (acall.get("deprecated"), acall.get("added"), acall.get("changed"))
    if deprecated or added or changed:
        retVal += "     * "
        if deprecated:
            if isinstance(deprecated, tuple):
                deprecated = " ".join(deprecated)
            retVal += "deprecated: " + deprecated + "&nbsp;&nbsp;&nbsp;"
        if added:
            if isinstance(added, tuple):
                added = " ".join(added)
            retVal += "added: " + added + "&nbsp;&nbsp;&nbsp;"
        if changed:
            if isinstance(changed, tuple):
                changed = " ".join(changed)
            retVal += "changed: " + changed + "&nbsp;&nbsp;&nbsp;"
            
    retVal += """<a href="" onclick="document.getElementById('{0}').style.display='none'; return false;"><b>hide</b></a><br/><br/></div>""".format(divid)
                   
    return retVal + '    */\n'

def categorize_query_options(query_opts, httpmeth):
    qopts_r = []
    qopts_o = []
    qopts_field = False
    
    if query_opts and httpmeth != "GET":
        print("query_opts on non GET:" + str(query_opts))
        raise Exception
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
        qoptparams += "String " + qoptr
    
    has_queryoptions = False
    if qopts_o:
        if qoptparams:
            qoptparams += ", "
        qoptparams += "String queryOptions"
        has_queryoptions = True
        
    #has_report_flavor = False
    for aparam in params:
        assert aparam in url_params or aparam == "REPORT_FLAVOR"
        if aparam == "REPORT_FLAVOR":
            assert qopts_field
        else:
            if qoptparams:
                qoptparams += ", "
            qoptparams += "String " + var_java_style(aparam)
    
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
                retVal += " + " + var_java_style(ppart[1:len(ppart) -1])
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
        raise Exception
    elif httpmeth in ["PUT", "POST"] and not datafields:
        #print("PUT/POST without data_fields: " + httpmeth + " " + path)
        pass
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




#options = {
#        "responseTypeConversion": "object that implements ResponseTypeConversion",
#        "indivoInstallation":
#           "foreignURL = indivoInstallation0[0]; consumerToken = indivoInstallation0[1]; consumerSecret = indivoInstallation0[2];",
#        "connectionTimeout": "integer",
#        "socketTimeout": "integer"
#        }

def process_calls(rest, pynames, report_flavors, python_meth_names):
    reports_minimal_records = False
    reports_minimal_carenet = False
    access_doc_map = {}
    call_count = 0
    method_count = 0
    dfekvm = {}
    retexs = {}
    retdscs = {}
    okcount = 0
    dividN = 0
    
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
        
        path0 = acall["path"]  #str(acall["path"])
        legged, response_form = api_skel_auxiliary.CALLS_AUX[(httpmeth,path0)]
                
        #legged = decide_oauth_legged(access_doc)
            
        # not used, just count occurrences in case the statistic is ever needed
        adcount = access_doc_map.get(access_doc)
        if adcount:
            access_doc_map[access_doc] = adcount +1
        else:
            access_doc_map[access_doc] = 1

        #response_form = decide_response_form(retex, retdsc, httpmeth, path)

        
        assert path0.startswith("/")
        path = path0[1:]
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
            #print("first_report_minimal: " + repr(pathparts))
            pathparts[len(pathparts) -2] = "{REPORT_FLAVOR}"   ############
            #print("first_report_minimal: " + repr(pathparts))

        
        method_name, params = make_method_name(pathparts, httpmeth)
        
        pmnpath = path0.replace("ACCOUNT_EMAIL", "ACCOUNT_ID")
        pmnpath = pmnpath.replace("PHA_EMAIL", "APP_ID")
        pmnpath = pmnpath.replace("SYSTEM_SHORT_NAME", "CODING_SYSTEM")
        pmnpath = pmnpath.replace("REQTOKEN_ID", "REQUEST_TOKEN")
        pmnpath = pmnpath.replace("DOCUMENT_ID_0", "DOCUMENT_ID")
        pmnpath = pmnpath.replace("DOCUMENT_ID_1", "OTHER_DOCUMENT_ID")
        pmnpath = pmnpath.replace("REL", "REL_TYPE")
        pmnpath = pmnpath.replace("OTHER_ACCOUNT_ID", "ACCOUNT_ID")
        pmn = python_meth_names.get((httpmeth.lower(), pmnpath))
        if not pmn:
            pmn = method_name   #use rest name if python name could no be determined
            print("python style name not found for: " + repr((httpmeth.lower(), pmnpath)) + "  " + path0)
        else:
            pmn = pmn[0]
            
        
        datafields = acall["data_fields"]
        assert isinstance(datafields, dict)
        put_post_data, put_post_data_form = decide_data_form(datafields, dfekvm, httpmeth, path)        

        write_both(rest, pynames,
            javadoc(
                params,
                #url_params,
                report_flavors,
                legged,
                put_post_data,
                put_post_data_form,
                response_form,
                dividN,
                acall)
        )
        dividN += 1
        
        query_opts = acall["query_opts"]
        qopts_r, qopts_o, qopts_field = categorize_query_options(query_opts, httpmeth)
            
        write_both(rest, pynames, "    public Object ")
        rest.write(method_name)
        pynames.write(pmn) 
        write_both(rest, pynames, "(\n            ") #)
        
        url_params = acall["url_params"]
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
        write_both(rest, pynames, mthprms)
                #==============
        write_both(rest, pynames, ') throws IndivoClientException {\n')
        
        if qopts_o:
            options_str1, options_str2 = process_query_opts(qopts_o, qopts_field, first_report_minimal, audit_query)
            write_both(rest, pynames, options_str1)
            write_both(rest, pynames, options_str2)
                
        write_both(rest, pynames, "        Object fromRequest = clientRequest(\n                ")
        write_both(rest, pynames, "\"" + httpmeth + "\", ")

        madeurl = make_url(pathparts)
        write_both(rest, pynames, madeurl)
        
        reqquer = request_query(qopts_r, qopts_o)
        write_both(rest, pynames, ", " + reqquer)
        
        if legged > 2:
            write_both(rest, pynames, ", accessToken, accessTokenSecret")
        else:
            write_both(rest, pynames, ", null, null")

        if put_post_data:
            write_both(rest, pynames, ", body")
            if put_post_data_form:
                if put_post_data_form == "url_encoded":
                    write_both(rest, pynames, ", \"application/x-www-form-urlencoded\"")
                elif put_post_data_form == "application/xml":
                    write_both(rest, pynames, ", \"application/xml\"")
                elif put_post_data_form == "text/plain":
                    write_both(rest, pynames, ", \"text/plain\"")
                else:
                    raise Exception
            else:
                write_both(rest, pynames, ", requestContentType")
                
        # "URL_ENCODED", "PLAIN_TEXT"
        if response_form == "unknown":
            write_both(rest, pynames, ", responseContentType")
        elif response_form == "XML":
            write_both(rest, pynames, ", \"application/xml\"")
        elif response_form == "JSON":
            write_both(rest, pynames, ", \"application/json\"")
        elif response_form == "URL_ENCODED":
            write_both(rest, pynames, ", \"application/x-www-form-urlencoded\"")
        elif response_form == "PLAIN_TEXT":
            write_both(rest, pynames, ", \"text/plain\"")
        else:
            raise Exception
        
        write_both(rest, pynames, ", options);\n        return fromRequest;\n")            
        write_both(rest, pynames, "    }\n\n")
        method_count += 1
        
    return call_count, method_count
#    print(qo_counts[0], common_qo[0])
#    print(qo_counts[1], common_qo[1])
#    print(qo_counts[2], common_qo[2])
#    print(qo_counts[3], common_qo[3])

#    print("report flavors: " + str(report_flavors))
#    for adv in access_doc_map.keys():
#        print adv + ": " + str(access_doc_map[adv])
#    print("call count: " + str(call_count))
#    
#    dfekvmK = dfekvm.keys()
#    for mK in dfekvmK:
#        print("datafields'': " + mK + ": " + str(dfekvm[mK]))
#
#    retexK = retexs.keys()
#    for rK in retexK:
#        print("response example: " + rK + ": " + str(retexs[rK]))
#
#    print("OK count: " + str(okcount))
#    retdscK = retdscs.keys()
#    for reK in retdscK:
#        print(str(retdscs[reK]) + ": response description: " + reK)


def write_both(file1, file2, content):
    file1.write(content)
    file2.write(content)

def writeprefix(prefixLines, rest, pynames):
    java_style_only = False;
    python_style_only = False;
    for pline in prefixLines:
        plinestr = str(pline)
        if plinestr.strip().startswith("/***START AUTO GENERATED FROM WIKI*/"):
            break
        shix = plinestr.find("_SHELL")
        if shix != -1:
            plinestr = plinestr[:shix] + plinestr[shix +6:]
        drix = plinestr.find("/*_DROP*/")
        if plinestr.find("/*_PYTHON_STYLE_ONLY*/") != -1:
            python_style_only = True;
            continue;
        elif plinestr.find("/*_END_PYTHON_STYLE_ONLY*/") != -1:
            python_style_only = False;
            continue;
        elif plinestr.find("/*_JAVA_STYLE_ONLY*/") != -1:
            java_style_only = True;
            continue;
        elif plinestr.find("/*_END_JAVA_STYLE_ONLY*/") != -1:
            java_style_only = False;
            continue;
            
        if drix == -1:
            if java_style_only:
                rest.write(plinestr)
            elif python_style_only:
                pynames.write(plinestr)
            else:
                write_both(rest, pynames, plinestr)

    
def writesuffix(rest, pynames, prefixLines):
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
                write_both(rest, pynames, plinestr)

def process_query_opts(query_opts0, qopts_field, report_minimal, audit_query):
    query_opts = list(query_opts0)
    query_opts.sort()
    qo_set = set(query_opts)
    if qo_set:
        if qo_set in common_qo:
            qoix = common_qo.index(qo_set)
            #qo_counts[qoix] += 1
            qo_set_name = common_qo_map.get(tuple(query_opts))
            optional_str_1 = ""
            optional_str_2a = "commonOptionsMap.get(\"" + qo_set_name + "\")"
        else:
            #print('unexpected query_opts0 set: ' + str(qo_set))
            optional_str_1 = "        List<String> optional = Arrays.asList("
            for qoki, qok in enumerate(query_opts):
                if qoki:
                    optional_str_1 += ", "
                optional_str_1 += '"' + qok + '"'
            optional_str_1 += ");\n"
            optional_str_2a = "optional"
            
        if report_minimal:
            optional_str_2b = "validQueryFields.get(reportFlavor), queryFieldType"
        elif audit_query:
            optional_str_2b = "allowedAuditQuery, null"
        else:
            optional_str_2b = "null, null"
            
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


restpath = sys.argv[1]
shellpath = sys.argv[2]
indivo_server_path = sys.argv[3]
rest0 =   open(restpath + "org/indivo/client/Rest.java", "w")
pynames =   open(restpath + "org/indivo/client/Rest_py_client_style.java", "w")
prefix = open(shellpath + "Rest_SHELL.java","r")

prefixSuffix = prefix.readlines()
writeprefix(prefixSuffix, rest0, pynames)
repflv, repqflds = get_report_flavors(indivo_server_path)

apidom = xml.dom.minidom.parse("../src/main/xml/api.xml")
python_meth_names = process_api_xml.process_dom(apidom)
apidom.unlink()
print("python meth names: " + str(len(python_meth_names.items())))

write_common_opts(rest0, pynames, repqflds)
#print("report flavors len: " + str(len(repflv)))
call_count, method_count = process_calls(rest0, pynames, repflv, python_meth_names)
writesuffix(rest0, pynames, prefixSuffix)
rest0.close()
pynames.close()
print("done. " + str(call_count) + " skeleton calls processed.  " + str(method_count) + " Java methods generated.")
