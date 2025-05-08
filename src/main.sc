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
                    a: Пожалуйста, продиктуйте свое имя
                    state: NoMatch
                        event: noMatch
                        script:
                            $dialer.setNoInputTimeout(1500);
                            $session.inputName = $request.query.trim();
                            $analytics.setSessionData("Имя", $session.inputName);
                            $reactions.transition("ConfirmName");
                        state: ConfirmName
                            a: Сохраняю ваше имя как {{$session.inputName}}. Всё верно?
                        state: NotCorrect
                            intent: /Отказ
                            go!: ../../../UpdatePhone
                        state: NewSurname
                            intent: /Согласие
                            a: Пожалуйста, продиктуйте вашу фамилию.
                            state: NoMatch
                                q: *
                                script:
                                    $dialer.setNoInputTimeout(1500);
                                    $session.surname = $request.query
                                    $analytics.setSessionData("Фамилия", $request.query);
                                    $reactions.transition("ConfirmUserName");
                                state: ConfirmUserName
                                    a: Сохраняю вашу фамилию как {{$session.surname}}. Всё верно?
                                    go: Check
                                    state: Check
                                        q: $agree || toState = "Correct"
                                        q: $disagree || toState = "NotCorrect"
                                        state: Correct
                                            a: Благодарю! Хорошего дня!
                                            script:
                                                $dialer.hangUp()
                                                                                    
                                        state: NotCorrect
                                            intent: $disagree
                                            go!: ../../../../../NewSurname
    
    state: No
        intent: /Отказ
        a: Хорошо, не буду отвлекать. Свяжемся позже — хорошего вам дня!
        
    state: What
        q: Какой у меня номер?
        script:
            $reactions.answer($dialer.getCaller());
        
    state: NoMatch
        event: noMatch
        a: Ой, я вас не поняла.