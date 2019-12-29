from database import db


class RequestedOffer(db.Model):
    __tablemane__ = "RequestedOffer"
    id = db.Column(db.String, unique=True, primary_key=True)
    serviceId = db.Column(db.String)
    userId = db.Column(db.String)
    request = db.Column(db.String)
    withUserParts = db.Column(db.Boolean)
    carType = db.Column(db.String)
    carModel = db.Column(db.String)
    carYear = db.Column(db.String)
    carVin = db.Column(db.String)
    serviceResponse = db.Column(db.String)
    servicePriceResponse = db.Column(db.String)
    fixStartDate = db.Column(db.String)
    fixEndDate = db.Column(db.String)
    serviceAcceptance = db.Column(db.Integer)
    userAcceptance = db.Column(db.Integer)

    def __repr__(self):
        return "id: {}, serviceId: {}, userId: {}, request: {}, withUserParts: {}, carType: {}, carModel: {}," \
               " carYear:{}, carVin: {}, serviceResponse: {}, servicePriceResponse: {}, fixStartDate: {}, " \
               "fixEndDate: {}, serviceAcceptance: {}, userAcceptance: {}" \
            .format(self.id, self.serviceId, self.userId, self.request, self.withUserParts, self.carType, self.carModel,
                    self.carYear, self.carVin, self.serviceResponse, self.servicePriceResponse, self.fixStartDate,
                    self.fixEndDate, self.serviceAcceptance, self.userAcceptance)

    def toDict(self):
        return dict(zip(["id", "serviceId", "userId", "request", "withUserParts", "carModel", "carYear", "carVin",
                         "serviceResponse", "servicePriceResponse", "fixStartDate", "fixEndDate", "serviceAcceptance",
                         "userAcceptance"], self.toList()))

    def toList(self):
        return [self.id, self.serviceId, self.userId, self.request, self.withUserParts, self.carType, self.carModel,
                self.carYear, self.carVin, self.serviceResponse, self.servicePriceResponse, self.fixStartDate,
                self.fixEndDate, self.serviceAcceptance, self.userAcceptance]
