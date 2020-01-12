from _ast import Store

from flask import send_file, send_from_directory, Response, url_for
from geoalchemy2 import Geometry
from sqlalchemy import func, asc, cast, text, or_
from sqlalchemy.orm import aliased
from sqlalchemy.sql import label
from werkzeug.utils import redirect
from geoalchemy2.comparator import Comparator

from API import *
from API.api_check import check_token
from database import db
from models.services import Service


@app.route("/services/getById/<serviceId>", methods=['GET'])
def getServiceById(serviceId):
    service = Service.query.filter_by(id=serviceId)
    if service.first():
        # print(service.first())
        serviceDict = service.first().toDict()
        serviceType = 0
        if 's' in serviceDict['serviceType']:
            serviceType |= 1
        if 't' in serviceDict['serviceType']:
            serviceType |= 2
        if 'c' in serviceDict['serviceType']:
            serviceType |= 4
        if 'i' in serviceDict['serviceType']:
            serviceType |= 8
        serviceDict['serviceType'] = serviceType
        return make_response(jsonify({"status": "Found",
                                      "service": serviceDict}),
                             status.HTTP_200_OK)
    return make_response(jsonify({"status": "NotFound"}), status.HTTP_404_NOT_FOUND)


@app.route("/services/addService", methods=["POST"])
@check_token
def createService():
    serviceId = unicode(uuid.uuid4())
    # image = base64.b64decode(request.json['imageEncoded'])
    # imagePath = os.path.join("Images", serviceId + "+" + request.json['serviceOwner'] + ".png")
    # f = open(imagePath, 'wb')
    # f.write(image)
    strServiceType = ""
    if int(request.json['serviceType']) & 1:
        strServiceType += "s"
    if int(request.json['serviceType']) & 2:
        strServiceType += "t"
    if int(request.json['serviceType']) & 4:
        strServiceType += "c"
    if int(request.json['serviceType']) & 8:
        strServiceType += "i"

    try:
        service = Service(id=serviceId,
                          logoPath=request.json['imageDownloadLink'],
                          name=request.json['serviceName'],
                          description=request.json['serviceDescription'],
                          latitude=request.json['latitude'],
                          longitude=request.json['longitude'],
                          address=request.json['serviceAddress'],
                          city=request.json['serviceCity'],
                          rating=0,
                          phoneNumber=request.json['servicePhone'],
                          email=request.json['serviceEmail'],
                          owner=request.json['serviceOwner'],
                          serviceType=strServiceType,
                          acceptedBrand=request.json['serviceAcceptedBrand'])
        print(service)
        db.session.add(service)
        db.session.commit()
    except Exception as e:
        print(e)
        return make_response(jsonify({"status": "Could not create"}), status.HTTP_409_CONFLICT)
    return make_response(jsonify({"status": "Created",
                                  "user": service.toDict()}),
                         status.HTTP_201_CREATED)


@app.route("/service/editService", methods=['PUT'])
def updateService():
    strServiceType = ""
    if int(request.json['serviceType']) & 1:
        strServiceType += "s"
    if int(request.json['serviceType']) & 2:
        strServiceType += "t"
    if int(request.json['serviceType']) & 4:
        strServiceType += "c"
    if int(request.json['serviceType']) & 8:
        strServiceType += "i"
    try:
        db.session.query(Service).filter(Service.id == request.json['serviceId']).update(
            {Service.name: request.json['serviceName'],
             Service.address: request.json['serviceAddress'],
             Service.city: request.json['serviceCity'],
             Service.phoneNumber: request.json['servicePhone'],
             Service.email: request.json['serviceEmail'],
             Service.description: request.json['serviceDescription'],
             Service.acceptedBrand: request.json['serviceAcceptedBrand'],
             Service.serviceType: strServiceType,
             Service.longitude: request.json['longitude'],
             Service.latitude: request.json['latitude'],
             Service.logoPath: request.json['imagePath']
             }, synchronize_session=False)
        db.session.commit()

    except Exception as e:
        print(e)
        return make_response(jsonify({"status": "Could not create"}), status.HTTP_409_CONFLICT)
    return make_response(jsonify({"status": "Updated"}),
                         status.HTTP_200_OK)


@app.route("/services/getIdsFilterBy/", methods=["GET"])
def getServiceIdsFilterBy():
    minRating = request.json['minRating']
    maxRating = request.json['maxRating']
    givenNameFilter = request.json['givenNameFilter']
    latitude = request.json['latitude']
    longitude = request.json['longitude']
    distanceInput = request.json['distanceInput']
    cityInput = request.json['cityInput']


def calculateDistance(lat1, long1, latitude, longitude, math=math):
    if (lat1 == latitude) and (long1 == longitude):
        return 0
    else:
        lat1 = math.radians(lat1)
        long1 = math.radians(long1)
        lat2 = math.radians(latitude)
        long2 = math.radians(longitude)
        earthRadius = 6371.01
        return earthRadius * math.acos(
            math.sin(lat1) * math.sin(lat2) + math.cos(lat1) * math.cos(lat2) * math.cos(long1 - long2))


def updateDistance(latitude, longitude):
    servicesId = db.session.query(Service.id).all()
    for i in servicesId:
        coords = db.session.query(Service.latitude, Service.longitude).filter_by(id=i[0]).first()
        distance = calculateDistance(float(coords[0]), float(coords[1]), float(latitude), float(longitude))
        db.session.query(Service).filter(Service.id == i[0]).update({Service.distanceFromUser: distance},
                                                                    synchronize_session=False)
        db.session.commit()


@app.route("/service/getIdsBetween/offset/<offset>/limit/<limit>", methods=['GET'])
def getIdsBetween(offset, limit):
    # updateDistance(latitude, longitude)
    # serviceIds = db.session.query(Service.id).order_by(asc(Service.distanceFromUser)).offset(offset).limit(limit).all()
    serviceIds = db.session.query(Service.id).offset(offset).limit(limit).all()
    print(serviceIds)
    return make_response(jsonify({"ids": serviceIds}), status.HTTP_200_OK)


@app.route("/services/getIdsBetweenForMyServices/<userId>/offset/<offset>/limit/<limit>", methods=['GET'])
@check_token
def getIdsBetweenForUser(userId, offset, limit):
    serviceIds = Service.query.with_entities(Service.id).filter_by(owner=userId).offset(offset).limit(limit).all()
    return make_response(jsonify({"ids": serviceIds}), status.HTTP_200_OK)


@app.route("/services/getIdsBetweenWithFilter/offset/<offset>/limit/<limit>/minRating/<minRating>/maxRating/<maxRating>"
           "/name/<name>/address/<address>/city/<city>/type/<serviceType>", methods=['GET'])
def getIdsBetweenWithFilter(offset, limit, minRating, maxRating, name, address, city, serviceType):
    if name == "empty":
        name = ''
    if address == "empty":
        address = ''
    if city == "empty":
        city = ''
    if int(serviceType) & 1:
        S = "s"
    else:
        S = "none"
    if int(serviceType) & 2:
        T = "t"
    else:
        T = "none"
    if int(serviceType) & 4:
        C = "c"
    else:
        C = "none"
    if int(serviceType) & 8:
        I = "i"
    else:
        I = "none"
    serviceIds = Service.query.with_entities(Service.id).filter(Service.rating >= minRating,
                                                                Service.rating <= maxRating,
                                                                Service.name.contains(name),
                                                                Service.address.contains(address),
                                                                Service.city.contains(city),
                                                                or_(Service.serviceType.contains(S),
                                                                    Service.serviceType.contains(T),
                                                                    Service.serviceType.contains(C),
                                                                    Service.serviceType.contains(I))
                                                                ).offset(
        offset).limit(
        limit).all()
    return make_response(jsonify({"ids": serviceIds}), status.HTTP_200_OK)


'''@app.route("/services/getInfoById/<serviceId>", methods=['GET'])
def getServiceInfo(serviceId):
    service = Service.query.filter_by(id=serviceId)
    if service.first():
        # print(service.first())
        serviceDict = service.first().toDict()
        del serviceDict["logoPath"]
        return make_response(jsonify({"status": "Found",
                                      "service": serviceDict}),
                             status.HTTP_302_FOUND)
    return make_response(jsonify({"status": "NotFound"}), status.HTTP_404_NOT_FOUND)


@app.route("/services/getImageById/<serviceId>", methods=['GET'])
def getServiceImage(serviceId):
    service = Service.query.filter_by(id=serviceId)
    if service.first():
        serviceDict = service.first().toDict()
        print(serviceDict["logoPath"])
        # return redirect(url_for('/services/getImageById/'+serviceId, filename=app.config['IMAGE_FOLDER'] + serviceDict["logoPath"]))
        print({"image": url_for("Images", filename="..\\" + serviceDict["logoPath"])})
        return make_response(jsonify(
            {"image": url_for("Images", filename=serviceDict["logoPath"].split("\\")[-1])}))
        # return send_file(app.config['IMAGE_FOLDER'] + serviceDict["logoPath"], mimetype='image/jpg',
        #                 attachment_filename='snapshot.png')

        return make_response(jsonify({"status": "NotFound"}), status.HTTP_404_NOT_FOUND)'''
