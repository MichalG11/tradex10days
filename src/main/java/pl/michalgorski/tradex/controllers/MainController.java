package pl.michalgorski.tradex.controllers;

import com.weddini.throttling.Throttling;
import com.weddini.throttling.ThrottlingType;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import pl.michalgorski.tradex.models.CurrencyModel;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Controller
public class MainController {


    @Throttling(type = ThrottlingType.RemoteAddr, limit = 1, timeUnit = TimeUnit.SECONDS)
    @GetMapping("/{currencyOne}")
    @ResponseBody
    public String showCurrency(@PathVariable("currencyOne") String currencyOne, Model model) {

        RestTemplate restTemplate = getRestTemplate();
        CurrencyModel currencyOneModel = restTemplate
                .getForObject("http://api.nbp.pl/api/exchangerates/rates/a/" + currencyOne + "/last/10/?format=json",
                        CurrencyModel.class);

        Optional<Double> maxCurrencyValueFromRest = currencyOneModel.getRates()
                .stream()
                .map(s -> s.getBid())
                .sorted((s, s1) -> Double.compare(s, s1) * -1)
                .limit(1)
                .findAny();

        double result = 0;

        if (maxCurrencyValueFromRest.isPresent()) {
            result = maxCurrencyValueFromRest.get();
        }

        return "Najwyższy kurs waluty z ostatnich 10 dni to : " + result;

    }

    @Bean
    private RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

}
