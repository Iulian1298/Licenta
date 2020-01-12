from API import *
from API.api_check import check_token
from database import db
from models.users import User


@app.route("/isLoggedIn", methods=['GET'])
@check_token
def isLoggedIn():
    return make_response(jsonify({'response': "Token OK"}), status.HTTP_202_ACCEPTED)


def getJWT(user):
    token = jwt.encode({'user': user, 'exp': datetime.datetime.utcnow() + datetime.timedelta(minutes=180)},
                       app.config["SECRET_KEY"]).decode("UTF-8")
    return token


@app.route("/register", methods=['POST'])
def authRegister():
    # ToDo: user profile image convert from base64 to image and save
    try:
        userId = unicode(uuid.uuid4())
        user = User(id=userId,
                    email=request.json['email'],
                    phoneNumber=request.json['phoneNumber'],
                    fullName=request.json['fullName'],
                    password=request.json['password'],
                    imagePath=request.json['imageDownloadLink'])
        print(user)
        db.session.add(user)
        db.session.commit()
    except Exception as e:
        print(e)
        return make_response(jsonify({"status": "Could not create"}), status.HTTP_409_CONFLICT)

    sendUser = user.toDict()
    del sendUser["imagePath"]
    return make_response(jsonify({"status": "Created",
                                  "token": getJWT(repr(user)),
                                  "user": sendUser}),
                         status.HTTP_201_CREATED)


@app.route("/login", methods=['POST'])
def authLogin():
    # ToDo: convert image in base64 and send in response
    email = request.json['email']
    password = request.json['password']
    user = User.query.filter_by(email=email)
    if user.first():
        if password == user.first().password:
            print(user.first().toDict())
            imageEncoded = ""
            userDict = user.first().toDict()
            # print(userDict)
            return make_response(jsonify({"status": "Found",
                                          "token": getJWT(repr(user)),
                                          "user": userDict}),
                                 status.HTTP_200_OK)
    return make_response(jsonify({"status": "Could not verify"}),
                         status.HTTP_401_UNAUTHORIZED)
