package com.springsource.html5expense.config;

import com.springsource.html5expense.integrations.EligibleChargeProcessor;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.scope.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.integration.MessageChannel;

import java.util.List;

/**
 * @author Josh Long
 */
@Configuration
@Import(ComponentConfig.class)
@ImportResource("/ec-loader.xml")
public class BatchConfig {

    @Autowired
    private ComponentConfig componentConfig;

    @Bean
    public SimpleJobLauncher jobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository((JobRepository) this.jobRepository().getObject());
        return jobLauncher;
    }

    @Bean public EligibleChargeProcessor eligibleChargeProcessor(){
        return new EligibleChargeProcessor ();
    }

    @Bean
    @Scope("step")
    public FlatFileItemReader reader(@Value("#{jobParameters[file]}") Resource resource) {

        DelimitedLineTokenizer del = new DelimitedLineTokenizer();
        del.setNames("date,amount,category,merchant".split(","));
        del.setDelimiter(DelimitedLineTokenizer.DELIMITER_COMMA);

        DefaultLineMapper defaultLineMapper = new DefaultLineMapper();
        defaultLineMapper.setLineTokenizer(del);

        FlatFileItemReader fileItemReader = new FlatFileItemReader();
        fileItemReader.setLineMapper(defaultLineMapper);
        fileItemReader.setResource(resource);

        return fileItemReader;
    }

    @Bean
    public ItemWriter writer() {
        return new MessageSendingItemWriter(this.channel);
    }

    @Bean
    public static StepScope stepScope() {
        return new StepScope();
    }

    @Bean
    public JobRepositoryFactoryBean jobRepository() throws Exception {
        JobRepositoryFactoryBean bean = new JobRepositoryFactoryBean();
        bean.setTransactionManager(this.componentConfig.transactionManager());
        bean.setDataSource(componentConfig.dataSource());
        return bean;
    }

    @Autowired
    @Qualifier("newEligibleCharges")
    private MessageChannel channel;

    public static class MessageSendingItemWriter implements ItemWriter<Object> {

        private MessageChannel channel;

        public MessageSendingItemWriter(MessageChannel channel) {
            this.channel = channel;
        }

        @Override
        public void write(List<? extends Object> objects) throws Exception {
            for (Object o : objects)
                System.out.println(ToStringBuilder.reflectionToString(o));
        }
    }


    static public void main(String args[]) throws Exception {
        AnnotationConfigApplicationContext annotationConfigApplicationContext =
                new AnnotationConfigApplicationContext(BatchConfig.class);

    }
}
