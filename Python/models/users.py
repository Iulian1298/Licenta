from database import db


class User(db.Model):
    _tablename_ = 'Users'
    id = db.Column(db.String, unique=True, primary_key=True)
    email = db.Column(db.String, unique=True, nullable=False)
    phoneNumber = db.Column(db.String)
    fullName = db.Column(db.String)
    password = db.Column(db.String)
    imageUrl = db.Column(db.String)

    def __repr__(self):
        return "id: {}, email: {}, phoneNumber: {}, fullName: {}, password: {}, imageUrl: {}" \
            .format(self.id, self.email, self.phoneNumber, self.fullName, self.password, self.imageUrl)

    def toDict(self):
        return dict(zip(["id", "email", "phoneNumber", "fullName", "imageUrl"], self.toList()))

    def toList(self):
        return [self.id, self.email, self.phoneNumber, self.fullName, self.imageUrl]
