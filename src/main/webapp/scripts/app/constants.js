'use strict';

angular.module('apqdApp')
    .constant('SecurityRole', {
        ADMIN: 'ROLE_ADMIN',
        CASE_WORKER: 'CASE_WORKER',
        PARENT: 'PARENT'
    })
    .constant('uibCustomDatepickerConfig', {
        formatYear: 'yyyy',
        startingDay: 1,
        showWeeks: false
    })
    .constant('FacilityType', {
        ADOPTION_AGENCY: {id: '01ft', name: 'ADOPTION AGENCY'},
        FOSTER_FAMILY_AGENCY: {id: '02ft', name: 'FOSTER FAMILY AGENCY'},
        FOSTER_FAMILY_AGENCY_SUB: {id: '03ft', name: 'FOSTER FAMILY AGENCY SUB'}
    })
    .constant('FacilityStatus', {
        LICENSED: {id: '01fs', name: 'LICENSED'},
        CLOSED: {id: '02fs', name: 'CLOSED'},
        PENDING: {id: '03fs', name: 'PENDING'},
        UNLICENSED: {id: '04fs', name: 'UNLICENSED'}
    });
