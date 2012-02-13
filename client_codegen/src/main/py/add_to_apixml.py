from __future__ import print_function
import xml.dom.minidom
import xml.dom

import api_skeleton
import api_skel_auxiliary
import use_ast

def process_dom(apidom):
    docel = apidom.documentElement
    assert docel.tagName == "api"
    
#    <api xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
# xsi:noNamespaceSchemaLocation="api_schema.xsd">

    docel.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
    docel.setAttribute("xsi:noNamespaceSchemaLocation", "api_schema.xsd")

    mdict = get_method_to_node_dict(docel)
    #add_from_
    
    report_dict = report_query_fields("/home/nate/indivo/github/indivo_server")
    for test_mdk in mdict.keys():
        test_mdv = mdict.get(test_mdk)
        if len(test_mdv) > 1:
            print("before merge_report_query_fields: " + str(len(test_mdv)) + test_mdv[0].toxml())
    merge_report_query_fields(report_dict, mdict)
    for test_mdk in mdict.keys():
        test_mdv = mdict.get(test_mdk)
        if len(test_mdv) > 1:
            print("after merge_report_query_fields: " + str(len(test_mdv)) + test_mdv[0].toxml())
        
    
    merge_skeleton_auxiliary(apidom, mdict)
    
    merge_api_skeleton(apidom, mdict)
    
    testout = open("testout.xml", 'w')
    apidom.writexml(testout)
    testout.close()
    
    

def get_method_to_node_dict(docel):    
    mdict = {}
    for child in docel.childNodes:
        if child.nodeType not in [xml.dom.Node.TEXT_NODE, xml.dom.Node.COMMENT_NODE]:
            if child.nodeType != xml.dom.Node.ELEMENT_NODE:
                print("nodetype: " + str(child.nodeType))
                raise Exception
            assert child.tagName == "method"
            
            process_method(child, mdict)
            
    return mdict
    



def process_method(methodNode, mdict):
    calls = methodNode.getElementsByTagName("call")
    assert calls.length == 1
    thecall = calls.item(0)
    #mname = str(thecall.getAttribute("name"))
    httpmeth = str(thecall.getAttribute("method"))
    murl  = str(thecall.getAttribute("url"))
    qix = murl.find('?')
    if qix != -1:
        assert murl[qix +1:] == "{PARAMETERS}"
        murl = murl[:qix]
        
    #print(mname, httpmeth, murl)
    fromdict = mdict.get((httpmeth,murl))
    if fromdict:
        print("adding second node: " + str(len(fromdict))  + "  " + fromdict[0].toxml() + "\n\n" + methodNode.toxml())
        fromdict.append(methodNode)
    else:
        mdict[(httpmeth,murl)] = [methodNode]

def merge_skeleton_auxiliary(apidom, mdict):
    for auxhttpmeth, auxpath0 in api_skel_auxiliary.CALLS_AUX.keys():
        pmnpath = skeleton_style_to_api_style(auxpath0)
        xmlnodes = mdict.get((auxhttpmeth.lower(), pmnpath))
        if not xmlnodes:
            print("python style name not found for: " + repr((auxhttpmeth.lower(), pmnpath)) + "  " + auxpath0)
        else:
            for xmln in xmlnodes:
#                if xmln.getElementsByTagName("call")[0].getAttribute("url").find("equipment") != -1:
#                    print("xmlnodes len: " + str(len(xmlnodes)) + "  -  " + xmln.toxml())
                legged, response_format = api_skel_auxiliary.CALLS_AUX.get((auxhttpmeth, auxpath0))
                oleg = apidom.createElement("oauth_legged")
                if legged <= 2.5:
                    oleg.setAttribute("two_legged", "two_legged")
                if legged >= 2.5:
                    oleg.setAttribute("three_legged", "three_legged")
                    
                elaftcall = None
                havecall = False
                for cnde in xmln.childNodes:
                    if (not havecall) and cnde.nodeType == xml.dom.Node.ELEMENT_NODE and cnde.tagName == "call":
                        havecall = True
                    elif havecall and  cnde.nodeType == xml.dom.Node.ELEMENT_NODE:
                        elaftcall = cnde
                        break
                    
                if elaftcall:
                    xmln.insertBefore(oleg, elaftcall)
                    xmln.insertBefore(apidom.createTextNode("\n    "), elaftcall)
                else:
                    xmln.appendChild(apidom.createTextNode("  "))
                    xmln.appendChild(oleg)
                    xmln.appendChild(apidom.createTextNode("\n  "))
                
                if response_format != "unknown":
                    response_nodes = []
                    xmln_cs = xmln.childNodes
                    for xmln_cn in xmln_cs:
                        if xmln_cn.nodeType == xml.dom.Node.ELEMENT_NODE and xmln_cn.tagName == "response":
                            response_nodes.append(xmln_cn)
                    if len(response_nodes) == 0:
                        print("missing response: " + xmln.toxml())
                        newresponse = apidom.createElement("response")
                        xmln.appendChild(apidom.createTextNode("  "))
                        xmln.appendChild(newresponse)
                        xmln.appendChild(apidom.createTextNode("\n  "))
                        response_nodes.append(newresponse)
                    if len(response_nodes) != 1:
                        print("more than one response: " + xmln.toxml())
                        raise Exception
                    #respnode = xmln.getElementsByTagName("response")
                    #if len(respnode) == 1:
                    if response_format == "ok":
                        response_format = "XML"
                        if not response_nodes[0].getAttribute("element"):
                            response_nodes[0].setAttribute("element", "ok")
                    response_nodes[0].setAttribute("response_format", response_format)
                    #else:

def skeleton_style_to_api_style(auxpath0):
        pmnpath = auxpath0.replace("ACCOUNT_EMAIL", "ACCOUNT_ID")
        pmnpath = pmnpath.replace("PHA_EMAIL", "APP_ID")
        pmnpath = pmnpath.replace("SYSTEM_SHORT_NAME", "CODING_SYSTEM")
        pmnpath = pmnpath.replace("REQTOKEN_ID", "REQUEST_TOKEN")
        pmnpath = pmnpath.replace("DOCUMENT_ID_0", "DOCUMENT_ID")
        pmnpath = pmnpath.replace("DOCUMENT_ID_1", "OTHER_DOCUMENT_ID")
        pmnpath = pmnpath.replace("REL", "REL_TYPE")
        pmnpath = pmnpath.replace("OTHER_ACCOUNT_ID", "ACCOUNT_ID")
        return pmnpath


#def get_report_flavors(indivo_server_path):
#    report_flavors = []        
#    for acall in api_skeleton.CALLS:
#        path = str(acall["path"])
#        assert path.startswith("/")
#        path = path[1:]
#        pathparts = path.split('/')
#        pplen = len(pathparts)
#        if (pplen == 6 and pathparts[pplen -1] == '' and pathparts[pplen -4] == "reports"
#             and pathparts[pplen -3] == "minimal" and pathparts[pplen -2][0] != '{'):
#            try:
#                report_flavors.index(pathparts[pplen -2])
#            except ValueError:
#                report_flavors.append(pathparts[pplen -2])
                
def report_query_fields(indivo_server_path):
    report_fields = use_ast.all_reports(indivo_server_path) #"/home/nate/indivo/github"
    report_fields_sn = {}
    for rfkey in report_fields.keys():
        #usix = rfkey.find('_FILTERS')
        rfkeyt = rfkey[0: len(rfkey) - len("_FILTERS")] # drop "_FILTERS"
        rfkeyt = rfkeyt.replace('_', '-')
        print("rfkey: " + rfkey + ' -- ' + rfkeyt)
        
        if rfkeyt in ["EQUIPMENT", "VITALS"]:
            pass
        elif rfkeyt == "ALLERGY":
            rfkeyt = "ALLERGIES"
        elif rfkeyt[len(rfkeyt) -1] != 'S':
            rfkeyt = rfkeyt + 'S'
        rfkeyt = rfkeyt.lower()
        report_fields_sn[rfkeyt] = report_fields[rfkey]
    return report_fields_sn

def merge_report_query_fields(report_dict, mdict): # mutate mdict
    print(str(len(report_dict.keys())))
    for rd in report_dict.keys():
        nodes_for_report = []
        #('get', '/records/{RECORD_ID}/reports/minimal/labs/')
        rnodes = mdict.get(("get", "/records/{RECORD_ID}/reports/minimal/" + rd + '/'))
        rnodes_care = mdict.get(("get", "/carenets/{CARENET_ID}/reports/minimal/" + rd + '/'))
        #'post', '/accounts/{ACCOUNT_ID}/authsystems/password/change'
        if not (rnodes or rnodes_care):
            rnodes = mdict.get(("get", "/records/{RECORD_ID}/reports/minimal/" + rd + "/{LAB_CODE}/"))
            rnodes_care = mdict.get(("get", "/carenets/{CARENET_ID}/reports/minimal/" + rd + "/{LAB_CODE}/"))
            
            
        if not (rnodes or rnodes_care):
            print("rd: " + "/records/{RECORD_ID}/reports/minimal/" + rd + '/')
            for amdk in mdict.keys():
                print(amdk)
            raise Exception
        else:
            for nfr in rnodes:
                nodes_for_report.append(nfr)
            for nfr in rnodes_care:
                nodes_for_report.append(nfr)
            if rd == "vitals":
                rnodes_vitals = mdict.get(("get", "/records/{RECORD_ID}/reports/minimal/vitals/{CATEGORY}/"))
                rnodes_vitals_care = mdict.get(("get", "/carenets/{CARENET_ID}/reports/minimal/vitals/{CATEGORY}")) # missing slash
                for rnv in rnodes_vitals:
                    nodes_for_report.append(rnv)
                for rnv in rnodes_vitals_care:
                    nodes_for_report.append(rnv)
                
            rdv = report_dict[rd]
            #[('medication_name', 'STRING'), ('medication_brand_name', 'STRING'), ('date_started', 'DATE'), ('date_stopped', 'DATE')]
            qfs = apidom.createElement("query_opts")
            for rdve in rdv:
                qfe = apidom.createElement("qopt")
                qfe.setAttribute("name", rdve[0])
                qfe.setAttribute("data_type", rdve[1])
                qfe.setAttribute("field", "field")
                qfs.appendChild(qfe)
                
            for rnode in nodes_for_report:
                rnode.appendChild(apidom.createTextNode("  "))
                rnode.appendChild(qfs.cloneNode(True))
                rnode.appendChild(apidom.createTextNode("\n  "))
                
            #print(repr(rdv))
            #raise Exception


def merge_api_skeleton(apidom, mdict):
    for acall in api_skeleton.CALLS:
        path0 = acall.get("path")
        path = skeleton_style_to_api_style(path0)
        httpmeth = acall.get("method")
        elems = mdict.get((httpmeth.lower(), path))
        if not elems:
            print("skeleton call not found in api: " + httpmeth  + "  " + path)
        else:
            query_options(acall.get("query_opts"), httpmeth, elems, apidom)
            decide_data_form(acall["data_fields"], httpmeth, path0, elems, apidom)
            
def query_options(qopts, httpmeth, elems, apidom):
        #qopts = acall.get("query_opts")
        qopts_r, qopts_o, qopts_field = categorize_query_options(qopts, httpmeth)
        if qopts_r or qopts_o or qopts_field:
            for elem in elems:
                qon = elem.getElementsByTagName("query_opts")
                if qon:
                    if len(qon) != 1:
                        print("expected 0 or one query_opts: " + repr(elem))
                    qon = qon[0]
                else:
                    qon = apidom.createElement("query_opts")
                    elem.appendChild(apidom.createTextNode("  "))
                    elem.appendChild(qon)
                    elem.appendChild(apidom.createTextNode("\n  "))
                    
                for reqo in qopts_r:
                    qopt = apidom.createElement("qopt")
                    qopt.setAttribute("name", reqo)
                    qopt.setAttribute("required", "required")
                    qon.appendChild(qopt)
                for opto in qopts_o:
                    qopt = apidom.createElement("qopt")
                    qopt.setAttribute("name", opto)
                    qon.appendChild(qopt)
                if qopts_field:
                    elemcall = elem.getElementsByTagName("call")[0]
                    elemurl = elemcall.getAttribute("url")
                    if elemurl.find("reports/minimal") == -1:
                        qopt = apidom.createElement("qopt")
                        qopt.setAttribute("field", "field")
                        qon.appendChild(qopt)
                    

def decide_data_form(datafields, httpmeth, path, elems, apidom):
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
                
    put_post_data = False
    put_post_data_form = None
    if dfkeys:
        #put_post_data = True
        ppdElem = apidom.createElement("put_post_data")
        if dfkeys[0]:
            #put_post_data_form = "url_encoded" # actual key/value pair expected
            ppdElem.setAttribute("format", "url_encoded")
            for dfkey in dfkeys:
                dfkeykey = apidom.createElement("key")
                dfkeykey.setAttribute("name", dfkey)
                ppdElem.appendChild(dfkeykey)
        else:
            assert len(dfkeys) == 1
            dfval = datafields[""].lower()
            if dfval.find("raw content") != -1:
                pass #put_post_data_form = None
            elif dfval.find("XML") != -1 or dfval.find("schema") != -1:
                #put_post_data_form = "application/xml"
                ppdElem.setAttribute("format", "application/xml")
            else:
                #put_post_data_form = "text/plain"
                ppdElem.setAttribute("format", "text/plain")
        
        for elem in elems:
            elem.appendChild(apidom.createTextNode("  "))
            elem.appendChild(ppdElem.cloneNode(True))
            elem.appendChild(apidom.createTextNode("\n  "))
    
            
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

    

if __name__ == "__main__":
    apidom = xml.dom.minidom.parse("../xml/api.xml")
    process_dom(apidom)
    apidom.unlink()
 
