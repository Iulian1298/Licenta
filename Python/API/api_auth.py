from API import *
from API.api_check import check_token
from database import db
from models.users import User


@app.route("/auth/checkLogged", methods=['GET'])
@check_token
def check():
    return make_response(jsonify({"response": "Token OK"}), status.HTTP_200_OK)


def getJWT(user):
    token = jwt.encode({'user': user},
                       app.config["SECRET_KEY"]).decode("UTF-8")
    return token


@app.route("/register", methods=['POST'])
def authRegister():
    try:
        userId = unicode(uuid.uuid4())
        user = User(id=userId,
                    email=request.json['email'],
                    phoneNumber=request.json['phoneNumber'],
                    fullName=request.json['fullName'],
                    password=request.json['password'],
                    imageUrl=request.json['imageDownloadLink'])
        print(user)
        db.session.add(user)
        db.session.commit()
    except Exception as e:
        print(e)
        return make_response(jsonify({"status": "Could not create"}), status.HTTP_409_CONFLICT)

    sendUser = user.toDict()
    del sendUser["imageUrl"]
    return make_response(jsonify({"status": "Created",
                                  "token": getJWT(repr(user)),
                                  "user": sendUser}),
                         status.HTTP_201_CREATED)


@app.route("/login", methods=['POST'])
def authLogin():
    email = request.json['email']
    password = request.json['password']
    user = User.query.filter_by(email=email)
    if user.first():
        if password == user.first().password:
            print(user.first().toDict())
            userDict = user.first().toDict()
            # print(userDict)
            return make_response(jsonify({"status": "Found",
                                          "token": getJWT(repr(user)),
                                          "user": userDict}),
                                 status.HTTP_200_OK)
    return make_response(jsonify({"status": "Could not verify"}),
                         status.HTTP_401_UNAUTHORIZED)


@app.route("/auth/changeProfile", methods=['PUT'])
@check_token
def changeProfile():
    user = User.query.filter_by(id=request.json['userId'])
    if user.first():
        if user.first().password == request.json['userOldPassword']:
            try:
                newPassword = request.json['userNewPassword']
                if newPassword == '':
                    newPassword = user.first().password
                db.session.query(User).filter(User.id == request.json['userId']).update(
                    {User.fullName: request.json['newUserFullname'],
                     User.email: request.json['newUserEmail'],
                     User.phoneNumber: request.json['newUserPhone'],
                     User.password: newPassword
                     }, synchronize_session=False)
                db.session.commit()

            except Exception as e:
                print(e)
                return make_response(jsonify({"status": "Could not edit"}), status.HTTP_409_CONFLICT)
            return make_response(jsonify({"status": "Updated"}),
                                 status.HTTP_200_OK)
        else:
            return make_response(jsonify({"status": "Could not verify"}),
                                 status.HTTP_401_UNAUTHORIZED)
