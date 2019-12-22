from flask import send_file, send_from_directory, Response

from API import *
from API.api_check import check_token
from database import db
from models.services import Service


@app.route("/services/getById/<serviceId>", methods=['GET'])
def getServiceById(serviceId):
    service = Service.query.filter_by(id=serviceId)
    if service.first():
        print(service.first())
        imageEncoded = ""
        serviceDict = service.first().toDict()
        with open(serviceDict["logoPath"], "rb") as image:
            imageEncoded = base64.b64encode(image.read()).decode('utf-8')
            serviceDict["imageEncoded"] = imageEncoded
        del serviceDict["logoPath"]
        return make_response(jsonify({"status": "Found",
                                      "service": serviceDict}),
                             status.HTTP_302_FOUND)
    return make_response(jsonify({"status": "NotFound"}), status.HTTP_404_NOT_FOUND)


@app.route("/services/getAllIds", methods=["GET"])
def getAllServiceIds():
    serviceIds = db.session.query(Service.id).all()
    return make_response(jsonify({"ids": serviceIds}), status.HTTP_200_OK)


@app.route("/services/addService", methods=["POST"])
def createService():
    image = base64.b64decode(request.json['imageEncoded'])
    imagePath = os.path.join("Images", request.json['name'] + request.json['owner'] + ".png")
    f = open(imagePath, 'wb')
    f.write(image)
    try:
        service = Service(id=unicode(uuid.uuid4()),
                          logoPath=imagePath,  # will be replaced with path of received image
                          name=request.json['name'],
                          description=request.json['description'],
                          latitude=request.json['lat'],
                          longitude=request.json['long'],
                          address=request.json['address'],
                          rating=request.json['rating'],
                          phoneNumber=request.json['phoneNumber'],
                          email=request.json['email'],
                          owner=request.json['owner'])
        print(service)
        db.session.add(service)
        db.session.commit()
    except Exception as e:
        print(e)
        return make_response(jsonify({"status": "Could not create"}), status.HTTP_409_CONFLICT)
    return make_response(jsonify({"status": "Created",
                                  "user": service.toDict()}),
                         status.HTTP_201_CREATED)


'''
@app.route("/services/getInfoById/<serviceId>", methods=['GET'])
def getServiceInfo(serviceId):
    service = Service.query.filter_by(id=serviceId)
    if service.first():
        print(service.first())
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
        return send_file(app.config['IMAGE_FOLDER'] + serviceDict["logoPath"], mimetype='image/jpeg')
    # return send_file(app.config['IMAGE_FOLDER'] + serviceDict["logoPath"], mimetype='image/jpg',
    #                 attachment_filename='snapshot.png')

    return make_response(jsonify({"status": "NotFound"}), status.HTTP_404_NOT_FOUND)
'''
