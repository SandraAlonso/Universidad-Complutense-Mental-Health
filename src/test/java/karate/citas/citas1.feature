Feature: csrf and log-out endpoint

Background:
* url baseUrl
* call read('../login/login3.feature')
* def util = Java.type('karate.KarateTests')


    Scenario: Get HTML page 'horarioPsicologo'
    Given path 'psicologo/horario/'
    When method get
    Then status 200
    * string response = response    
    * def h1s = util.selectHtml(response, "div.center-align");
    * print h1s
    And match h1s contains 'Anterior semana'