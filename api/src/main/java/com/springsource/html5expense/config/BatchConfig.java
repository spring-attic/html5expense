package com.springsource.html5expense.config;

import com.springsource.html5expense.integrations.EligibleChargeProcessor;
import com.springsource.html5expense.integrations.EligibleChargeProcessorHeaders;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
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

    private File batchFileDirectory;

    @Autowired
    public void setBatchFileDirectory( @Value("#{ systemProperties['user.home'] }")  String userHome) throws Exception {
        this.batchFileDirectory = new File(userHome , "in");
    }

    @Bean
    public SimpleJobLauncher jobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository((JobRepository) this.jobRepository().getObject());
        return jobLauncher;
    }

    @Bean
    public EligibleChargeProcessor eligibleChargeProcessor() {
        return new EligibleChargeProcessor();
    }

    @Bean
    @Scope("step")
    public FlatFileItemReader reader( @Value("#{jobParameters[file]}") String  resource) {

        File f = new File( this.batchFileDirectory, resource  + ".csv");

        DelimitedLineTokenizer del = new DelimitedLineTokenizer();
        del.setNames("date,amount,category,merchant".split(","));
        del.setDelimiter(DelimitedLineTokenizer.DELIMITER_COMMA);

        DefaultLineMapper <FieldSet> defaultLineMapper = new DefaultLineMapper<FieldSet>();
        defaultLineMapper.setLineTokenizer(del);
        defaultLineMapper.setFieldSetMapper(new PassThroughFieldSetMapper());
        defaultLineMapper.afterPropertiesSet();

        FlatFileItemReader <FieldSet> fileItemReader = new FlatFileItemReader<FieldSet>();
        fileItemReader.setLineMapper(defaultLineMapper);
        fileItemReader.setResource( new FileSystemResource( f) );

        return fileItemReader;
    }

    @Bean
    public ItemWriter writer() {
        return new MessageSendingItemWriter(this.channel);
    }
    @Bean
    public JobRepositoryFactoryBean jobRepository() throws Exception {
        JobRepositoryFactoryBean bean = new JobRepositoryFactoryBean();
        bean.setTransactionManager(new DataSourceTransactionManager(componentConfig.dataSource()));
        bean.setDataSource(componentConfig.dataSource());
        return bean;
    }

    @Autowired
    @Qualifier("newEligibleCharges")
    private MessageChannel channel;

    public static class MessageSendingItemWriter implements ItemWriter<DefaultFieldSet > {

        private MessageChannel channel;

        public MessageSendingItemWriter(MessageChannel channel) {
            this.channel = channel;
        }

        @Override
        public void write(List<? extends DefaultFieldSet> defaultFieldSets) throws Exception {
          for(DefaultFieldSet defaultFieldSet : defaultFieldSets)
          {
              Date date  = defaultFieldSet.readDate(0) ;
              String merchant = defaultFieldSet.readString(1);
              String category = defaultFieldSet.readString(2);
              BigDecimal bigDecimal = defaultFieldSet.readBigDecimal(3);

              Message msg = MessageBuilder.withPayload( category)
                      .setHeader(EligibleChargeProcessorHeaders.EC_AMOUNT, bigDecimal)
                      .setHeader(EligibleChargeProcessorHeaders.EC_CATEGORY, category)
                      .setHeader(EligibleChargeProcessorHeaders.EC_MERCHANT, merchant)
                      .setHeader(EligibleChargeProcessorHeaders.EC_DATE, date)
                      .build();
              this.channel.send(msg);

          }
        }
    }


    public static void main(String args[]) throws Exception {

        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(BatchConfig.class);

        Job job = annotationConfigApplicationContext.getBean("read-eligible-charges", Job.class);

        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString("file", "foo");
        builder.addLong("uid", System.currentTimeMillis());
        JobParameters jobParameters = builder.toJobParameters();

        JobLauncher jobLauncher = annotationConfigApplicationContext.getBean(JobLauncher.class);
        jobLauncher.run(job, jobParameters);

    }
}
