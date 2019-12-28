from API import *
from API.api_check import check_token
from database import db
from models.lockedDay import LockedDay
from models.lockedHour import LockedHour


@app.route("/addLockedPeriod", methods=['POST'])
# @check_token
def addLockedPeriod():
    try:
        lockedPeriod = {}
        lockedDay = LockedDay.query.filter_by(day=request.json['day'])
        if lockedDay.first():
            newLockedHoursNr = lockedDay.first().toDict()['lockedHours'] + 1
            db.session.query(LockedDay).filter(LockedDay.day == request.json['day']).update(
                {LockedDay.lockedHours: newLockedHoursNr}, synchronize_session=False)

            lockedHour = LockedHour(id=unicode(uuid.uuid4()),
                                    dayId=lockedDay.first().toDict()['id'],
                                    ownerId=request.json['ownerId'],
                                    hour=request.json['hour'])
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
                                    hour=request.json['hour'])
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


@app.route("/getLockedDays", methods=['GET'])
# @check_token
def getLockedDays():
    result = LockedDay.query.with_entities(LockedDay.day).filter_by(lockedHours=10).all()
    if result:
        lockedDays = [i[0] for i in result]
        return make_response(jsonify({"lockedDays": lockedDays}), status.HTTP_200_OK)
    else:
        return make_response(jsonify({"lockedDays": []}), status.HTTP_200_OK)


@app.route("/getLockedDays/<day>", methods=['GET'])
# @check_token
def getLockedHoursForDay(day):
    dayId = LockedDay.query.with_entities(LockedDay.id).filter_by(day=day)
    lockedHours = []
    if dayId.first():
        result = LockedHour.query.with_entities(LockedHour.hour).filter_by(dayId=dayId[0][0]).all()
        if result:
            lockedHours = [i[0] for i in result]
    return make_response(jsonify({"lockedHours": lockedHours}), status.HTTP_200_OK)
