from database import db


class Service(db.Model):
    _tablename_ = 'Services'
    id = db.Column(db.String, unique=True, primary_key=True)
    logoPath = db.Column(db.String)
    name = db.Column(db.String)
    description = db.Column(db.String)
    latitude = db.Column(db.Float)
    longitude = db.Column(db.Float)
    address = db.Column(db.String)
    rating = db.Column(db.Float)
    phoneNumber = db.Column(db.String)
    email = db.Column(db.String)
    owner = db.Column(db.String)
    serviceType = db.Column(db.Integer)
    acceptedBrand = db.Column(db.String)

    def __repr__(self):
        return "id: {}, logoPath: {}, name: {}, description: {}, latitude: {},longitude: {}, address: {}, " \
               "rating: {}, phoneNumber: {}, email: {}, owner: {}, serviceType: {},acceptedBrand: {}" \
            .format(self.id, self.logoPath, self.name, self.description, self.latitude, self.longitude, self.address,
                    self.rating, self.phoneNumber, self.email, self.owner, self.serviceType, self.acceptedBrand)

    def toDict(self):
        return dict(zip(
            ["id", "logoPath", "name", "description", "latitude", "longitude", "address",
             "rating", "phoneNumber", "email", "owner", "serviceType", "acceptedBrand"], self.toList()))

    def toList(self):
        return [self.id, self.logoPath, self.name, self.description, self.latitude, self.longitude, self.address,
                self.rating, self.phoneNumber, self.email, self.owner, self.serviceType, self.acceptedBrand]
