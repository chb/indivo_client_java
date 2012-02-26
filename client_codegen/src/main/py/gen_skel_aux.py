import api_skeleton

def decide_response_form(retex, retdsc, httpmeth, path):
    """ returns "XML", "JSON", "URL_ENCODED", "PLAIN_TEXT" or "unknown" """
    retVal = "unknown"
    
    #print(path, retex, retdsc)
    
    #</ok> (sic)
    if retex == "<ok/>" or retex == "</ok>":
        retVal = "ok" 
    elif (retex[0] == '<' and
         (retdsc.startswith(":http:statuscode:`200` with a list of ")
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
            or retdsc.find("`200` with metadata on the") != -1
            or retdsc.find("`200` with metadata describing the") != -1
            or retdsc.find("`200` with the document metadata") != -1
            or retdsc.find("`200` with the document's metadata") != -1
            or retdsc.find("`200` with the metadata on the new") != -1
            or retdsc.find("`200` with the metadata on the updated") != -1
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
    #print(access_doc)
    legged = None
    if access_doc in [
        "A user app with an id matching the app email in the URL.",
        "Any admin app.",
        "Any Indivo UI app.",
        "An admin app with an id matching the principal_email in the URL.",
        "A request signed by a RequestToken.",
        "Any principal in Indivo.",
        "The user app itself."]:
        legged=2.0
    elif access_doc in [
"A principal in full control of the record, or any admin app.",
#"Any principal in Indivo.",
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

def process_calls(auxo):
    auxo.write("CALLS_AUX = {\n    ")
    first = True
    for acall in api_skeleton.CALLS:
        httpmeth = acall["method"]
        assert httpmeth in ["GET", "PUT", "POST", "DELETE"]
        
        retex = acall["return_ex"].strip()
        assert isinstance(retex, str)
#        retex50 = retex.strip()[:50]

        retdsc = acall["return_desc"].strip()
        assert isinstance(retdsc, str)
        
        cwalo = ":http:statuscode:`200`, with a list of "
        if retdsc.startswith(cwalo):
            retdsc = ":http:statuscode:`200` with a list of " + retdsc[len(cwalo):]

        path = acall["path"]  #str(acall["path"])        
#        if path != "/accounts/{ACCOUNT_EMAIL}/inbox/":
#            continue
                        
        access_doc = acall["access_doc"]
        legged = decide_oauth_legged(access_doc)
            
        response_form = decide_response_form(retex, retdsc, httpmeth, path)
        
        if first:
            first = False
        else:
            auxo.write(",\n    ")
        auxo.write("(\"" + httpmeth + "\", \"" + path + "\") : (" + str(legged) + ", \"" + response_form + "\")")

    auxo.write("\n}\n")

auxo = open("api_skel_auxiliary.py", 'w')
auxo.write("# each dict key is a URL from api_skeleton.py,\n")
auxo.write("# each dict value is a 2-item tupple: (oauth_leggedness, responseFormat), where:\n")
auxo.write("#    oauth_leggedness is 2.0, 2.5 or 3.0.  2.5 means both 2-legged and 3-legged calls ar possible\n")
auxo.write("#    resonseFormat is \"XML\", \"JSON\", \"URL_ENCODED\", \"PLAIN_TEXT\", or \"unknown\".\n")
process_calls(auxo)
