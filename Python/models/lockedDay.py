from database import db


class LockedDay(db.Model):
    _tablename_ = 'LockedDay'
    id = db.Column(db.String, unique=True, primary_key=True)
    serviceId = db.Column(db.String)
    day = db.Column(db.String)
    lockedHours = db.Column(db.Integer)

    def __repr__(self):
        return "id: {}, serviceId: {}, day: {}, lockedHours: {}" \
            .format(self.id, self.serviceId, self.day, self.lockedHours)

    def toDict(self):
        return dict(zip(["id", "serviceId", "day", "lockedHours"], self.toList()))

    def toList(self):
        return [self.id, self.serviceId, self.day, self.lockedHours]
