from sqlalchemy import desc

from API import *
from API.api_check import check_token
from database import db
from models.requestedOffer import RequestedOffer
from models.services import Service
from models.users import User


@app.route("/requestedOffers/addRequestedOffer", methods=['POST'])
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
                                        userAcceptance=0,
                                        addedDate=datetime.date.today(),
                                        deletedByUser=False,
                                        deletedByService=False,
                                        seen=1)
        print(requestedOffer)
        db.session.add(requestedOffer)
        db.session.commit()
    except Exception as e:
        print(e)
        return make_response(jsonify({"status": "Could not create"}), status.HTTP_409_CONFLICT)
    return make_response(jsonify({"status": "Created",
                                  "requestedOffer": requestedOffer.toDict()}), status.HTTP_201_CREATED)


@app.route("/requestedOffers/getMyRequestsIds/<userId>/limit/<limit>/offset/<offset>", methods=['GET'])
@check_token
def getMyRequestsIds(userId, limit, offset):
    myRequestIds = [i[0] for i in
                    RequestedOffer.query.with_entities(RequestedOffer.id).filter_by(userId=userId,
                                                                                    deletedByUser=False).order_by(
                        desc(RequestedOffer.addedDate)).offset(offset).limit(
                        limit).all()]
    # print(myRequestIds)
    return make_response(jsonify({"Ids": myRequestIds}), status.HTTP_200_OK)


@app.route("/requestedOffers/getById/<requestId>/userOrService/<isUserOrService>", methods=['GET'])
@check_token
def getByIdForUserOrService(requestId, isUserOrService):
    request = RequestedOffer.query.filter_by(id=requestId)
    if request.first():
        requestDict = request.first().toDict()
        userName = User.query.filter_by(id=requestDict["userId"]).first().toDict()["fullName"]
        serviceName = Service.query.filter_by(id=requestDict["serviceId"]).first().toDict()["name"]
        if int(isUserOrService) == 1:
            servicePhone = Service.query.filter_by(id=requestDict["serviceId"]).first().toDict()["phoneNumber"]
            requestDict["servicePhoneNumber"] = servicePhone
        else:
            userPhone = User.query.filter_by(id=requestDict["userId"]).first().toDict()["phoneNumber"]
            requestDict["userPhoneNumber"] = userPhone
        requestDict["userName"] = userName
        requestDict["serviceName"] = serviceName
        # print(requestDict)
        return make_response(jsonify({"status": "Found", "request": requestDict}), status.HTTP_200_OK)
    return make_response(jsonify({"status": "NotFound"}), status.HTTP_404_NOT_FOUND)


@app.route("/requestedOffers/getServiceRequestsIds/<serviceId>/limit/<limit>/offset/<offset>", methods=['GET'])
@check_token
def getServiceRequestsIds(serviceId, limit, offset):
    myRequestIds = [i[0] for i in
                    RequestedOffer.query.with_entities(RequestedOffer.id).filter_by(serviceId=serviceId,
                                                                                    deletedByService=False).order_by(
                        desc(RequestedOffer.addedDate)).offset(offset).limit(
                        limit).all()]
    # print(myRequestIds)
    return make_response(jsonify({"Ids": myRequestIds}), status.HTTP_200_OK)


@app.route("/requestedOffers/addServiceResponse", methods=['POST'])
@check_token
def addServiceResponse():
    serviceAcceptance = request.json['serviceAcceptance']
    if int(serviceAcceptance) == 1:
        requestOffer = RequestedOffer.query.filter_by(id=request.json['requestId'])
        if requestOffer.first():
            db.session.query(RequestedOffer).filter(RequestedOffer.id == request.json['requestId']) \
                .update({RequestedOffer.fixStartDate: request.json['startDate'],
                         RequestedOffer.fixEndDate: request.json['endDate'],
                         RequestedOffer.servicePriceResponse: request.json['price'],
                         RequestedOffer.serviceAcceptance: serviceAcceptance,
                         RequestedOffer.addedDate: datetime.date.today(),
                         RequestedOffer.seen: 2}, synchronize_session=False)
            db.session.commit()
            return make_response(jsonify({"status": "Response added"}), status.HTTP_200_OK)
        else:
            return make_response(jsonify({"status": "Could not add response"}), status.HTTP_404_NOT_FOUND)
    else:
        if int(serviceAcceptance) == 2:
            requestOffer = RequestedOffer.query.filter_by(id=request.json['requestId'])
            if requestOffer.first():
                db.session.query(RequestedOffer).filter(RequestedOffer.id == request.json['requestId']) \
                    .update({RequestedOffer.serviceResponse: request.json['serviceResponse'],
                             RequestedOffer.serviceAcceptance: serviceAcceptance,
                             RequestedOffer.addedDate: datetime.date.today(),
                             RequestedOffer.seen: 2}, synchronize_session=False)
                db.session.commit()
                return make_response(jsonify({"status": "Response added"}), status.HTTP_200_OK)
            else:
                return make_response(jsonify({"status": "Could not add response"}), status.HTTP_404_NOT_FOUND)


@app.route("/requestedOffers/deleteRequestForServiceById/<requestId>", methods=['DELETE'])
@check_token
def deleteRequestForService(requestId):
    try:
        requestOffer = RequestedOffer.query.filter_by(id=requestId)
        if requestOffer.first():
            if requestOffer.first().toDict()['deletedByUser'] == 1:
                requestOffer.delete()
                db.session.commit()
            else:
                db.session.query(RequestedOffer).filter(RequestedOffer.id == requestId).update(
                    {RequestedOffer.deletedByService: 1,
                     RequestedOffer.addedDate: datetime.date.today()}, synchronize_session=False)
                db.session.commit()
        return make_response(jsonify({"status": "Request offer deleted"}), status.HTTP_200_OK)
    except Exception as e:
        print(e)
        return make_response(jsonify({"status": "Could not delete"}), status.HTTP_409_CONFLICT)


@app.route("/requestedOffers/seenBy/<userOrService>/requestOfferId/<requestId>", methods=["PUT"])
@check_token
def seenBy(userOrService, requestId):
    db.session.query(RequestedOffer).filter(RequestedOffer.id == requestId) \
        .update({RequestedOffer.seen: RequestedOffer.seen + int(userOrService),
                 RequestedOffer.addedDate: datetime.date.today()}, synchronize_session=False)
    db.session.commit()
    return make_response(jsonify({"status": "Request offer seen updated"}), status.HTTP_200_OK)


@app.route("/requestedOffers/hasUpdate/<userId>")
@check_token
def hasUserUpdate(userId):
    requestOffer = RequestedOffer.query.filter_by(userId=userId, seen=2)
    if requestOffer.first():
        return make_response(jsonify({"response": "Has Update"}), status.HTTP_200_OK)
    else:
        return make_response(jsonify({"response": "No Update"}), status.HTTP_200_OK)


@app.route("/requestedOffers/hasRequests/<userId>")
@check_token
def hasNewRequests(userId):
    userServices = Service.query.filter_by(owner=userId)
    for i in userServices.all():
        requestOffer = RequestedOffer.query.filter_by(serviceId=i.toDict()['id'], seen=1)
        if requestOffer.first():
            return make_response(jsonify({"response": "Has Update"}), status.HTTP_200_OK)

    return make_response(jsonify({"response": "No Update"}), status.HTTP_200_OK)


@app.route("/requestedOffer/addClientResponse/<userAcceptance>/forRequest/<requestId>", methods=['PUT'])
@check_token
def addClientResponse(userAcceptance, requestId):
    db.session.query(RequestedOffer).filter(RequestedOffer.id == requestId) \
        .update({RequestedOffer.userAcceptance: int(userAcceptance),
                 RequestedOffer.addedDate: datetime.date.today(),
                 RequestedOffer.seen: 1}, synchronize_session=False)
    db.session.commit()
    return make_response(jsonify({"status": "Request offer seen updated"}), status.HTTP_200_OK)


@app.route("/requestedOffers/deleteRequestForClientById/<requestId>", methods=['DELETE'])
@check_token
def deleteRequestForClient(requestId):
    try:
        requestOffer = RequestedOffer.query.filter_by(id=requestId)
        if requestOffer.first():
            if int(requestOffer.first().toDict()['serviceAcceptance']) != 0:
                db.session.query(RequestedOffer).filter(RequestedOffer.id == requestId).update(
                    {RequestedOffer.deletedByUser: 1,
                     RequestedOffer.userAcceptance: 2,
                     RequestedOffer.addedDate: datetime.date.today(),
                     RequestedOffer.seen: 1}, synchronize_session=False)
                db.session.commit()
            else:
                requestOffer.delete()
                db.session.commit()
    except Exception as e:
        print(e)
        return make_response(jsonify({"status": "Could not delete"}), status.HTTP_409_CONFLICT)
    return make_response(jsonify({"status": "Deleted"}), status.HTTP_200_OK)
