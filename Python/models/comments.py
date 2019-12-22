from database import db


class Comment(db.Model):
    _tablename_ = 'Comments'
    id = db.Column(db.String, unique=True, primary_key=True)
    userId = db.Column(db.String)
    serviceId = db.Column(db.String)
    comment = db.Column(db.String)
    creationTime = db.Column(db.String)
    rating = db.Column(db.Float)

    def __repr__(self):
        return "id: {}, userId: {}, serviceId: {}, comment: {}, creationTime: {}, rating: {}" \
            .format(self.id, self.userId, self.serviceId, self.comment, self.creationTime, self.rating)

    def toDict(self):
        return dict(zip(["id", "userId", "serviceId", "comment", "creationTime", "rating"], self.toList()))

    def toList(self):
        return [self.id, self.userId, self.serviceId, self.comment, self.creationTime, self.rating]
