require: patterns.sc
  module = sys.zb-common

require: dicts/Employees.csv
    name = Employees
    var = $Employees
    
require: dicts/EmployeesEmphasis.csv
    name = EmployeesEmphasis
    var = $EmployeesEmphasis
    
require: functions.js

theme: /

    state: Start
        q!: $regex</start>
        script:
            sleep(1500);
            $dialer.setTtsConfig({emotion: "good"});
        a: Здравствуйте! Я Инара-Секретарь.
        a: Мы уточняем и актуализируем контактную информацию наших сотрудников. Подскажите, удобно ли сейчас говорить?

    state: Yes
        intent: /Согласие
        go!: CheckEmployeeExists
        state: CheckEmployeeExists
            script:
                $session.name = FindEmployeeByNumber($dialer.getCaller(), $Employees, $EmployeesEmphasis);
                if ($session.name) {
                    $reactions.transition("CheckData"); 
                } else {
                    $reactions.transition("NoName");
                }
            state: NoName
                a: Ваш номер отсуствует в нашей базе данных.
                go!: UpdatePhone
            state: CheckData
                a: У нас указан ваш номер на имя {{$session.name}}. Всё верно?
                state: EndThanks
                    intent: /Согласие
                    a: Благодарю! Хорошего дня!
                    script:
                        $dialer.hangUp()
                state: UpdatePhone
                    intent: /Отказ
                    a: Для обновления нашей базы данных нам необходимо ваше имя и фамилия.
                    a: Пожалуйста, продиктуйте полностью ваше имя и фамилию. Сначала имя, а потом фамилию.
                    state: GetFullName
                        q: *
                        script:
                            $dialer.setNoInputTimeout(2000);
                            var fullName = $request.query.trim();
                            var parts = fullName.split(/\s+/);
                            if (parts.length >= 2) {
                                $session.inputName = parts[0];
                                $session.surname = parts[1];
                            } else {
                                $session.inputName = fullName;
                                $session.surname = "";
                            }
                            $session.fullNameRaw = fullName;
                            $analytics.setSessionData("ФИО", fullName);
                            $reactions.transition("ConfirmFullName");
                        state: ConfirmFullName
                            a: Сохраняю ваше имя как {{$session.inputName}}, фамилию как {{$session.surname}}. Всё верно?
                            go: Check
                            state: Check
                                q: $agree || toState = "Correct"
                                q: $disagree || toState = "NotCorrect"
                                state: Correct
                                    a: Благодарю! Хорошего дня!
                                    script:
                                        $dialer.hangUp()
                                state: NotCorrect
                                    a: Повторите, пожалуйста, полностью ваше имя и фамилию.
                                    go!: ../../UpdatePhone
    
    state: No
        intent: /Отказ
        a: Хорошо, не буду отвлекать. Свяжемся позже — хорошего вам дня!
        
    state: What
        q: Какой у меня номер?
        script:
            $reactions.answer($dialer.getCaller());
    
    state: NoMatch
        event!: noMatch
        a: Ой, я вас не поняла, 

    state: NoInput || noContext = true
        event!: speechNotRecognized
        script:
            $session.noInputCounter = $session.noInputCounter || 0;
            $session.noInputCounter++;
        if: $session.noInputCounter >= 3
            a: Кажется, проблемы со связью.
            script:
                $dialer.hangUp();
        else:
            a: Вас плохо слышно. Повторите, пожалуйста!