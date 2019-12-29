from API import *
from API.api_check import check_token
from database import db
from models.requestedOffer import RequestedOffer


@app.route("/addRequestedOffer", methods=['POST'])
@check_token
def addRequestedOffer():
    try:
        requestedOfferId = unicode(uuid.uuid4())
        requestedOffer = RequestedOffer(id=requestedOfferId,
                                        serviceId=request.json['serviceId'],
                                        userId=request.json['userId'],
                                        request=request.json['request'],
                                        withUserParts=bool(request.json['withUserParts']),
                                        carType=request.json['carType'],
                                        carModel=request.json['carModel'],
                                        carYear=request.json['carYear'],
                                        carVin=request.json['carVin'],
                                        serviceResponse=" ",
                                        servicePriceResponse=" ",
                                        fixStartDate=" ",
                                        fixEndDate=" ",
                                        serviceAcceptance=0,
                                        userAcceptance=0)
        print(requestedOffer)
        db.session.add(requestedOffer)
        db.session.commit()
    except Exception as e:
        print(e)
        return make_response(jsonify({"status": "Could not create"}), status.HTTP_409_CONFLICT)
    return make_response(jsonify({"status": "Created",
                                  "requestedOffer": requestedOffer.toDict()}), status.HTTP_201_CREATED)
