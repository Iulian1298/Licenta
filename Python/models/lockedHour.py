from database import db


class LockedHour(db.Model):
    _tablename_ = 'LockedHour'
    __table_args__ = (db.UniqueConstraint('dayId', 'hour', name='unique_day_hour'),)
    id = db.Column(db.String, unique=True, primary_key=True)
    dayId = db.Column(db.String)
    ownerId = db.Column(db.String)
    hour = db.Column(db.String)
    shortDescription = db.Column(db.String)
    scheduleType = db.Column(db.Integer)

    def __repr__(self):
        return "id: {}, dayId: {}, ownerId: {}, hour: {}, shortDescription: {}, scheduleType" \
            .format(self.id, self.dayId, self.ownerId, self.hour, self.shortDescription, self.scheduleType)

    def toDict(self):
        return dict(zip(["id", "dayId", "ownerId", "hour", "shortDescription", "scheduleType"], self.toList()))

    def toList(self):
        return [self.id, self.dayId, self.ownerId, self.hour, self.shortDescription, self.scheduleType]
