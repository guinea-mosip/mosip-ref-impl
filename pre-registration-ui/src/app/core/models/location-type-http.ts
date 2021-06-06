export interface LocationTypeHttp {
    id: string;
    errors: string;
    metadata: string;
    response: LocationValue[];
    responsetime: string;
    version: string;
}

export interface LocationValue {
    code: string;
    hierarchyLevel: number;
    hierarchyName: string;
    isActive: boolean;
    langCode: string;
    name: string;
    parentLocCode: string;
}