function FindEmployeeByNumber(phone, EmployeesList, EmphasisList){
    var Name = "";
        // log(EmployeesList);
    //log('phone to find - ' + phone);
    Object.keys(EmployeesList).forEach(function(key) {
        // log(EmployeesList[key].value)
        if (EmployeesList[key].value.telephone == phone){
            // log(EmployeesList[key].value.name);
            Name = FindCorrectEmphasis(EmployeesList[key].value.name, EmphasisList);
            return;
        }
            //$reactions.answer(EmployeesList[key].value.name);

    });    
    return Name;
}

function FindCorrectEmphasis(name,EmphasisList)
{
    var correct_emphasis =  name;
        Object.keys(EmphasisList).forEach(function(key) 
        {
            if (EmphasisList[key].value.text == name){
                correct_emphasis = EmphasisList[key].value.emphasis ;
            }
        });

    return correct_emphasis;
}
