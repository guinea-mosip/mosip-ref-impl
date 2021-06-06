import { AttributeModel } from './attribute.modal';

/**
 * @description This is the data object for the request object for adding the user.
 * @author Shashank Agrawal
 *
 * @export
 * @class IdentityModel
 */
export class IdentityModel {
  constructor(
    public IDSchemaVersion: number,
    public firstName: AttributeModel[],
    public lastName: AttributeModel[],
    public dateOfBirth: string,
    public gender: AttributeModel[],
    public residenceStatus: AttributeModel[],
    public additionalAddressDetails: string,
    public region: AttributeModel[],
    public prefecture: AttributeModel[],
    public subPrefectureOrCommune: AttributeModel[],
    public district: AttributeModel[],
    public sector: AttributeModel[],
    public phone: string,
    public email: string,
  ) {}
}
