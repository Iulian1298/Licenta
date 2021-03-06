from sqlalchemy import asc, desc

from API import *
from API.api_check import check_token
from database import db
from models.lockedDay import LockedDay
from models.lockedHour import LockedHour
from models.services import Service
from models.users import User


@app.route("/addLockedPeriod", methods=['POST'])
@check_token
def addLockedPeriod():
    lockedDay = LockedDay.query.filter_by(day=request.json['day'], serviceId=request.json['serviceId'])
    if lockedDay.first():
        numberOfScheduledHour = LockedHour.query.filter_by(dayId=lockedDay.first().toDict()['id'],
                                                           ownerId=request.json['ownerId'])
        if numberOfScheduledHour.first() != None:
            return make_response(jsonify({"status": "Could not create"}), status.HTTP_403_FORBIDDEN)
    try:
        lockedPeriod = {}
        if lockedDay.first():
            newLockedHoursNr = lockedDay.first().toDict()['lockedHours'] + 1
            db.session.query(LockedDay).filter(LockedDay.day == request.json['day'],
                                               LockedDay.serviceId == request.json['serviceId']).update(
                {LockedDay.lockedHours: newLockedHoursNr}, synchronize_session=False)

            lockedHour = LockedHour(id=unicode(uuid.uuid4()),
                                    dayId=lockedDay.first().toDict()['id'],
                                    ownerId=request.json['ownerId'],
                                    hour=request.json['hour'],
                                    shortDescription=request.json['shortDescription'],
                                    scheduleType=request.json['scheduleType'])
            lockedPeriod['lockedDay'] = lockedDay.first().toDict()
            lockedPeriod['lockedHour'] = lockedHour.toDict()
            db.session.add(lockedHour)
        else:
            dayId = unicode(uuid.uuid4())
            lockedDay = LockedDay(id=dayId,
                                  serviceId=request.json['serviceId'],
                                  day=request.json['day'],
                                  lockedHours=1)
            lockedHour = LockedHour(id=unicode(uuid.uuid4()),
                                    dayId=dayId,
                                    ownerId=request.json['ownerId'],
                                    hour=request.json['hour'],
                                    shortDescription=request.json['shortDescription'],
                                    scheduleType=request.json['scheduleType'])
            lockedPeriod['lockedDay'] = lockedDay.toDict()
            lockedPeriod['lockedHour'] = lockedHour.toDict()
            db.session.add(lockedDay)
            db.session.add(lockedHour)

        db.session.commit()
    except Exception as e:
        print(e)
        return make_response(jsonify({"status": "Could not create"}), status.HTTP_409_CONFLICT)
    return make_response(jsonify({"status": "Created",
                                  "lockedPeriod": lockedPeriod}),
                         status.HTTP_201_CREATED)


@app.route("/getLockedDays/<serviceId>", methods=['GET'])
@check_token
def getLockedDays(serviceId):
    result = LockedDay.query.with_entities(LockedDay.day).filter_by(lockedHours=8, serviceId=serviceId).all()
    if result:
        lockedDays = [i[0] for i in result]
        return make_response(jsonify({"lockedDays": lockedDays}), status.HTTP_200_OK)
    else:
        return make_response(jsonify({"lockedDays": []}), status.HTTP_200_OK)


@app.route("/getLockedHoursForDay/<day>/serviceId/<serviceId>", methods=['GET'])
@check_token
def getLockedHoursForDay(day, serviceId):
    dayId = LockedDay.query.with_entities(LockedDay.id).filter_by(day=day, serviceId=serviceId)
    lockedHours = []
    if dayId.first():
        result = LockedHour.query.with_entities(LockedHour.hour).filter_by(dayId=dayId[0][0]).all()
        if result:
            lockedHours = [i[0] for i in result]
    return make_response(jsonify({"lockedHours": lockedHours}), status.HTTP_200_OK)


@app.route("/getLockedHoursForTodayForService/<serviceId>", methods=['GET'])
@check_token
def getLockedHoursForToday(serviceId):
    day = date.today()
    dayId = LockedDay.query.with_entities(LockedDay.id).filter_by(day=day, serviceId=serviceId)
    result = []
    if dayId.first():
        lockedHours = LockedHour.query.filter_by(dayId=dayId.first()[0]).order_by(asc(LockedHour.hour)).all()
        for i in lockedHours:
            content = {}
            content['username'] = User.query.with_entities(User.fullName).filter_by(id=i.toDict()['ownerId']).first()[0]
            content['phoneNumber'] = \
                User.query.with_entities(User.phoneNumber).filter_by(id=i.toDict()['ownerId']).first()[0]
            content['hour'] = i.toDict()['hour']
            content['shortDescription'] = i.toDict()['shortDescription']
            content["appointmentId"] = i.toDict()['id']
            content["appointmentType"] = i.toDict()['scheduleType']
            content['day'] = str(day)
            result.append(content)
        print((result))
    return make_response(jsonify({"appointment": result}), status.HTTP_200_OK)


@app.route("/lockedPeriod/getMyAppointmentsIds/<userId>/limit/<limit>/offset/<offset>", methods=['GET'])
@check_token
def getMyAppointmentsIds(userId, limit, offset):
    appointmentsIds = [i[0] for i in
                       LockedHour.query.with_entities(LockedHour.id).filter_by(ownerId=userId).all()]
    appointmentsIds.reverse()
    appointmentsIds = appointmentsIds[int(offset): int(offset) + int(limit)]
    return make_response(jsonify({"Ids": appointmentsIds}), status.HTTP_200_OK)


@app.route("/lockedPeriod/getById/<appointmentId>", methods=['GET'])
@check_token
def getAppointmentById(appointmentId):
    lockedHours = LockedHour.query.filter_by(id=appointmentId).first()
    lockedDay = LockedDay.query.filter_by(id=lockedHours.toDict()["dayId"]).first()
    content = {}
    content['serviceName'] = Service.query.filter_by(id=lockedDay.toDict()["serviceId"]).first().toDict()["name"]
    content['dayId'] = lockedHours.toDict()["dayId"]
    content['hour'] = lockedHours.toDict()["hour"]
    content['shortDescription'] = lockedHours.toDict()["shortDescription"]
    content['appointmentType'] = lockedHours.toDict()["scheduleType"]
    content['phoneNumber'] = Service.query.filter_by(id=lockedDay.toDict()["serviceId"]).first().toDict()[
        "phoneNumber"]
    content['day'] = lockedDay.toDict()['day']
    print(content)
    return make_response(jsonify({"appointment": content}), status.HTTP_200_OK)


@app.route("/lockedPeriod/deleteById/<appointmentId>", methods=['DELETE'])
@check_token
def deleteAppointmentById(appointmentId):
    try:
        appointment = LockedHour.query.filter_by(id=appointmentId)
        lockedDay = LockedDay.query.filter_by(id=appointment.first().toDict()['dayId'])
        if lockedDay.first().toDict()['lockedHours'] == 1:
            lockedDay.delete()
        else:
            lockedDay.update({LockedDay.lockedHours: LockedDay.lockedHours - 1}, synchronize_session=False)
        appointment.delete()
        db.session.commit()
    except Exception as e:
        print(e)
        return make_response(jsonify({"status": "Could not delete"}), status.HTTP_409_CONFLICT)
    return make_response(jsonify({"status": "Deleted"}), status.HTTP_200_OK)
