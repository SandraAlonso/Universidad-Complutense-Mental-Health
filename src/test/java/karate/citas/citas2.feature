Feature: csrf and log-out endpoint

Background:
* url baseUrl
* call read('citas1.feature')
* def util = Java.type('karate.KarateTests')

Scenario: user page
    Given path 'psicologo/horario/saveAppointment'
    And form field name = 'terapia1'
    And form field date = '17/06/2020'
     And form field start_hour = '14:14'
    And form field finish_hour = '15:15'
     And form field description = 'nueva terapia'
    When method get
    Then status 200
    * def id = response
